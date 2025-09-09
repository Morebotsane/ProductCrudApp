package com.example.services;

import com.example.dao.AddressDAO;
import com.example.dao.CustomerDAO;
import com.example.dto.AddressRequest;
import com.example.dto.AddressResponse;
import com.example.dto.mappers.CustomerAddressMapper;
import com.example.entities.Address;
import com.example.entities.AddressType;
import com.example.entities.Customer;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerAddressServiceTest {

    @InjectMocks
    private CustomerAddressService addressService;

    @Mock private AddressDAO addressDAO;
    @Mock private CustomerDAO customerDAO;
    @Mock private CustomerAddressMapper addressMapper;
    @Mock private AuditService auditService;
    @Mock private SecurityContext securityContext;
    @Mock private JsonWebToken jwt;
    @Mock private EntityManager entityManager;

    private Customer customer;
    private Address existingAddress;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock EntityManager flush
        when(addressDAO.getEntityManager()).thenReturn(entityManager);
        doNothing().when(entityManager).flush();

        // Sample customer
        customer = new Customer("John", "Doe", "john@example.com", "+266 58359036");
        customer.setId(1L);

        // Sample existing address
        existingAddress = new Address();
        existingAddress.setId(100L);
        existingAddress.setCustomer(customer);

        // Default security mocks
        when(jwt.getName()).thenReturn("system");
    }

    // -------------------------
    // ADD ADDRESS
    // -------------------------
    @Test
    void testAddAddress_success() {
        AddressRequest request = new AddressRequest();
        request.setLine1("123 Main St");
        request.setCity("Metropolis");
        request.setPostalCode("12345");
        request.setCountry("Neverland");
        request.setType(AddressType.SHIPPING);

        Address newAddress = new Address();
        newAddress.setCustomer(customer);

        AddressResponse response = new AddressResponse(
                1L, "123 Main St", null, "Metropolis", null, "12345", "Neverland", AddressType.SHIPPING, false
        );

        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);
        when(addressMapper.toEntity(request)).thenReturn(newAddress);
        when(addressMapper.toResponse(newAddress)).thenReturn(response);
        doNothing().when(addressDAO).save(newAddress);

        // Simulate customer role
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(false);
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
        when(jwt.getSubject()).thenReturn("1");

        AddressResponse result = addressService.addAddress(1L, request);

        assertNotNull(result);
        assertEquals("123 Main St", result.getLine1());
        verify(addressDAO).save(newAddress);
        verify(entityManager).flush();
        verify(auditService).record(eq("system"), eq("ADD_ADDRESS"), eq("CustomerAddress"), any(), contains("\"customerId\": 1"));
    }

    @Test
    void testAddAddress_OtherCustomer_ThrowsSecurityException() {
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(false);
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
        when(jwt.getSubject()).thenReturn("2"); // different user

        AddressRequest request = new AddressRequest();

        SecurityException ex = assertThrows(SecurityException.class,
                () -> addressService.addAddress(1L, request));

        assertEquals("Forbidden: Cannot access another customer's addresses", ex.getMessage());
    }

    // -------------------------
    // GET ADDRESSES
    // -------------------------
    @Test
    void testGetAddresses_success() {
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);
        when(addressDAO.findByCustomer(customer)).thenReturn(List.of(existingAddress));
        when(addressMapper.toResponse(existingAddress)).thenReturn(
                new AddressResponse(100L, "123 Main St", null, "Metropolis", null, "12345", "Neverland", AddressType.SHIPPING, false)
        );

        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);

        List<AddressResponse> addresses = addressService.getAddresses(1L);

        assertEquals(1, addresses.size());
        assertEquals("123 Main St", addresses.get(0).getLine1());
    }

    @Test
    void testGetAddresses_OtherCustomer_ThrowsSecurityException() {
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(false);
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
        when(jwt.getSubject()).thenReturn("2");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> addressService.getAddresses(1L));

        assertEquals("Forbidden: Cannot access another customer's addresses", ex.getMessage());
    }

    // -------------------------
    // UPDATE ADDRESS
    // -------------------------
    @Test
    void testUpdateAddress_success() {
        AddressRequest request = new AddressRequest();
        request.setLine1("456 Elm St");
        request.setCity("Gotham");
        request.setPostalCode("67890");
        request.setCountry("Neverland");
        request.setType(AddressType.SHIPPING);
        request.setDefault(true);

        AddressResponse response = new AddressResponse(100L, "456 Elm St", null, "Gotham", null, "67890", "Neverland", AddressType.SHIPPING, true);

        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);
        when(addressDAO.findById(Address.class, 100L)).thenReturn(existingAddress);
        when(addressMapper.toResponse(existingAddress)).thenReturn(response);
        doNothing().when(addressDAO).update(existingAddress);

        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);

        AddressResponse updated = addressService.updateAddress(1L, 100L, request);

        assertNotNull(updated);
        assertEquals("456 Elm St", updated.getLine1());
        assertTrue(updated.isDefault());
        verify(addressDAO).update(existingAddress);
        verify(entityManager).flush();
        verify(auditService).record(eq("system"), eq("UPDATE_ADDRESS"), eq("CustomerAddress"), eq(100L), contains("\"customerId\": 1"));
    }

    @Test
    void testUpdateAddress_OtherCustomer_ThrowsSecurityException() {
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(false);
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
        when(jwt.getSubject()).thenReturn("2");

        AddressRequest request = new AddressRequest();

        SecurityException ex = assertThrows(SecurityException.class,
                () -> addressService.updateAddress(1L, 100L, request));

        assertEquals("Forbidden: Cannot access another customer's addresses", ex.getMessage());
    }

    // -------------------------
    // DELETE ADDRESS
    // -------------------------
    @Test
    void testDeleteAddress_success() {
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);
        when(addressDAO.findById(Address.class, 100L)).thenReturn(existingAddress);
        doNothing().when(addressDAO).delete(existingAddress);

        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);

        boolean deleted = addressService.deleteAddress(1L, 100L);

        assertTrue(deleted);
        verify(addressDAO).delete(existingAddress);
        verify(entityManager).flush();
        verify(auditService).record(eq("system"), eq("DELETE_ADDRESS"), eq("CustomerAddress"), eq(100L), contains("\"customerId\": 1"));
    }

    @Test
    void testDeleteAddress_OtherCustomer_ThrowsSecurityException() {
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(false);
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
        when(jwt.getSubject()).thenReturn("2");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> addressService.deleteAddress(1L, 100L));

        assertEquals("Forbidden: Cannot access another customer's addresses", ex.getMessage());
    }
}
