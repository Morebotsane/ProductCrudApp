package com.example.services;

import com.example.dao.AddressDAO;
import com.example.dao.CustomerDAO;
import com.example.dto.AddressRequest;
import com.example.dto.AddressResponse;
import com.example.dto.mappers.CustomerAddressMapper;
import com.example.entities.Address;
import com.example.entities.AddressType;
import com.example.entities.Customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerAddressServiceTest {

    @InjectMocks
    private CustomerAddressService addressService;

    @Mock
    private AddressDAO addressDAO;

    @Mock
    private CustomerDAO customerDAO;

    @Mock
    private CustomerAddressMapper addressMapper;

    private Customer customer;
    private Address existingAddress;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup a customer with ID
        customer = new Customer("John", "Doe", "john@example.com","+266 58359036");
        customer.setId(1L);

        // Setup existing address
        existingAddress = new Address();
        existingAddress.setCustomer(customer);
        existingAddress.setId(100L);
    }

    @Test
    void addAddress_success() {
        AddressRequest request = new AddressRequest();
        request.setLine1("123 Main St");
        request.setCity("Metropolis");
        request.setPostalCode("12345");
        request.setCountry("Neverland");
        request.setType(AddressType.SHIPPING);

        Address newAddress = new Address();
        newAddress.setCustomer(customer);

        AddressResponse response = new AddressResponse(
                1L, "123 Main St", null, "Metropolis", null, "12345", "Neverland",
                AddressType.SHIPPING, false
        );

        // Mock DAOs and Mapper
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);
        when(addressMapper.toEntity(request)).thenReturn(newAddress);
        when(addressMapper.toResponse(newAddress)).thenReturn(response);
        doNothing().when(addressDAO).save(newAddress);

        AddressResponse result = addressService.addAddress(1L, request);

        assertNotNull(result);
        assertEquals("123 Main St", result.getLine1());
        verify(addressDAO).save(newAddress);
    }

    @Test
    void updateAddress_success() {
        AddressRequest request = new AddressRequest();
        request.setLine1("456 Elm St");
        request.setCity("Gotham");
        request.setPostalCode("67890");
        request.setCountry("Neverland");
        request.setType(AddressType.SHIPPING);

        AddressResponse response = new AddressResponse(
                100L, "456 Elm St", null, "Gotham", null, "67890", "Neverland",
                AddressType.SHIPPING, true
        );

        // Mock DAO and mapper
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);
        when(addressDAO.findById(Address.class, 100L)).thenReturn(existingAddress);
        doNothing().when(addressDAO).update(existingAddress);
        when(addressMapper.toResponse(existingAddress)).thenReturn(response);

        // Mark as default in request
        request.setDefault(true);

        AddressResponse updated = addressService.updateAddress(1L, 100L, request);

        assertNotNull(updated);
        assertEquals("456 Elm St", updated.getLine1());
        assertTrue(updated.isDefault());
        verify(addressDAO).update(existingAddress);
    }

    @Test
    void deleteAddress_success() {
        // Mock DAO
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);
        when(addressDAO.findById(Address.class, 100L)).thenReturn(existingAddress);
        doNothing().when(addressDAO).delete(existingAddress);

        boolean deleted = addressService.deleteAddress(1L, 100L);

        assertTrue(deleted);
        verify(addressDAO).delete(existingAddress);
    }

    @Test
    void getAddresses_success() {
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);
        when(addressDAO.findByCustomer(customer)).thenReturn(List.of(existingAddress));
        when(addressMapper.toResponse(existingAddress)).thenReturn(
                new AddressResponse(
                        100L, "123 Main St", null, "Metropolis", null, "12345", "Neverland",
                        AddressType.SHIPPING, false
                )
        );

        List<AddressResponse> addresses = addressService.getAddresses(1L);

        assertEquals(1, addresses.size());
        assertEquals("123 Main St", addresses.get(0).getLine1());
    }
}
