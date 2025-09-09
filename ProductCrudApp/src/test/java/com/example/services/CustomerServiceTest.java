package com.example.services;

import com.example.dao.CustomerDAO;
import com.example.dto.CustomerRequest;
import com.example.dto.CustomerResponse;
import com.example.dto.PaginatedResponse;
import com.example.entities.Customer;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock private CustomerDAO customerDAO;
    @Mock private AuditService auditService;
    @Mock private SecurityContext securityContext;
    @Mock private jakarta.ws.rs.core.SecurityContext dummySecurityContext;
    @Mock private org.eclipse.microprofile.jwt.JsonWebToken jwt;
    @Mock private EntityManager entityManager;

    private Customer customer1;
    private Customer customer2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Ensure flush() won't throw NPE
        when(customerDAO.getEntityManager()).thenReturn(entityManager);
        doNothing().when(entityManager).flush();

        // Sample customers
        customer1 = new Customer("John", "Doe", "john@example.com", "1234567890");
        customer1.setId(1L);
        customer2 = new Customer("Jane", "Smith", "jane@example.com", "0987654321");
        customer2.setId(2L);

        // Default security mocks
        when(jwt.getName()).thenReturn("system");
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);
    }

    // -------------------------
    // GET CUSTOMERS
    // -------------------------
    @Test
    void testGetCustomers_withFilter() {
        List<Customer> customers = Arrays.asList(customer1, customer2);
        when(customerDAO.findCustomers(0, 10, "example")).thenReturn(customers);
        when(customerDAO.countCustomers("example")).thenReturn(2L);

        PaginatedResponse<CustomerResponse> response = customerService.getCustomers(1, 10, "example");

        assertNotNull(response);
        assertEquals(2, response.getItems().size());
        assertEquals(2, response.getTotalItems());
        assertEquals(1, response.getCurrentPage());

        verify(customerDAO).findCustomers(0, 10, "example");
        verify(customerDAO).countCustomers("example");
    }

    @Test
    void testGetCustomers_ForbiddenForNonAdmin() {
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(false);

        SecurityException ex = assertThrows(SecurityException.class,
                () -> customerService.getCustomers(1, 10, null));

        assertEquals("Forbidden: Only admins can list customers", ex.getMessage());
    }

    // -------------------------
    // GET CUSTOMER BY ID
    // -------------------------
    @Test
    void testGetCustomerById_found() {
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer1);
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);

        CustomerResponse response = customerService.getCustomerById(1L);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    void testGetCustomerById_notFound() {
        when(customerDAO.findById(Customer.class, 99L)).thenReturn(null);

        CustomerResponse response = customerService.getCustomerById(99L);
        assertNull(response);
    }

    @Test
    void testGetCustomerById_OtherCustomer_ThrowsSecurityException() {
        when(customerDAO.findById(Customer.class, 2L)).thenReturn(customer2);
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(false);
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
        when(jwt.getSubject()).thenReturn("1"); // current user is customer1

        SecurityException ex = assertThrows(SecurityException.class,
                () -> customerService.getCustomerById(2L));

        assertEquals("Forbidden: Cannot access another customer's data", ex.getMessage());
    }

    // -------------------------
    // CREATE CUSTOMER
    // -------------------------
    @Test
    void testCreateCustomer_success() {
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);

        CustomerRequest request = new CustomerRequest("Alice", "Wonder", "alice@example.com", "5555555555");

        doAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setId(10L); // simulate DB-generated ID
            return null;
        }).when(customerDAO).save(any(Customer.class));

        CustomerResponse response = customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals("Alice", response.getFirstName());
        verify(customerDAO).save(any(Customer.class));
        verify(auditService).record(eq("system"), eq("CREATE_CUSTOMER"), eq("Customer"), eq(10L), contains("alice@example.com"));
    }

    @Test
    void testCreateCustomer_ForbiddenForNonCustomerRole() {
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(false);

        CustomerRequest request = new CustomerRequest("Alice", "Wonder", "alice@example.com", "5555555555");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> customerService.createCustomer(request));

        assertEquals("Forbidden: Only customers can create accounts", ex.getMessage());
    }

    // -------------------------
    // UPDATE CUSTOMER
    // -------------------------
    @Test
    void testUpdateCustomer_found() {
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer1);
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);

        CustomerRequest request = new CustomerRequest("John", "Updated", "john@new.com", "1112223333");
        CustomerResponse response = customerService.updateCustomer(1L, request);

        assertNotNull(response);
        assertEquals("Updated", response.getLastName());
        assertEquals("john@new.com", response.getEmail());
        verify(customerDAO).update(customer1);
        verify(auditService).record(eq("system"), eq("UPDATE_CUSTOMER"), eq("Customer"), eq(1L), contains("john@new.com"));
    }

    @Test
    void testUpdateCustomer_notFound() {
        when(customerDAO.findById(Customer.class, 99L)).thenReturn(null);

        CustomerRequest request = new CustomerRequest("Test", "User", "test@user.com", "0001112222");
        CustomerResponse response = customerService.updateCustomer(99L, request);

        assertNull(response);
        verify(customerDAO, never()).update(any());
        verify(auditService, never()).record(anyString(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void testUpdateCustomer_OtherCustomer_ThrowsSecurityException() {
        when(customerDAO.findById(Customer.class, 2L)).thenReturn(customer2);
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(false);
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
        when(jwt.getSubject()).thenReturn("1");

        CustomerRequest request = new CustomerRequest("Jane", "Updated", "jane@new.com", "0001112222");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> customerService.updateCustomer(2L, request));

        assertEquals("Forbidden: Cannot access another customer's data", ex.getMessage());
    }

    // -------------------------
    // DELETE CUSTOMER
    // -------------------------
    @Test
    void testDeleteCustomer_found() {
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer1);
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);

        boolean result = customerService.deleteCustomer(1L);

        assertTrue(result);
        verify(customerDAO).delete(customer1);
        verify(auditService).record(eq("system"), eq("DELETE_CUSTOMER"), eq("Customer"), eq(1L), anyString());
    }

    @Test
    void testDeleteCustomer_notFound() {
        when(customerDAO.findById(Customer.class, 99L)).thenReturn(null);

        boolean result = customerService.deleteCustomer(99L);

        assertFalse(result);
        verify(customerDAO, never()).delete(any());
        verify(auditService, never()).record(anyString(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void testDeleteCustomer_OtherCustomer_ThrowsSecurityException() {
        when(customerDAO.findById(Customer.class, 2L)).thenReturn(customer2);
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(false);
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
        when(jwt.getSubject()).thenReturn("1");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> customerService.deleteCustomer(2L));

        assertEquals("Forbidden: Cannot access another customer's data", ex.getMessage());
    }
}
