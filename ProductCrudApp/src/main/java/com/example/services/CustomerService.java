package com.example.services;

import com.example.dao.CustomerDAO;
import com.example.dto.CustomerRequest;
import com.example.dto.CustomerResponse;
import com.example.dto.PaginatedResponse;
import com.example.dto.mappers.CustomerMapper;
import com.example.entities.Customer;
import com.example.security.JwtTokenService;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class CustomerService {

    @Inject private CustomerDAO customerDAO;
    @Inject private AuditService auditService;
    @Inject private JwtTokenService jwtService;

    // -------------------------
    // LIST CUSTOMERS
    // -------------------------
    public PaginatedResponse<CustomerResponse> getCustomers(int page, int size, String emailFilter) {
        requireAdmin("Only admins can list customers");

        int offset = Math.max(0, (page - 1) * size);
        List<Customer> customers = customerDAO.findCustomers(offset, size, emailFilter);

        List<CustomerResponse> dtoList = customers.stream()
                .map(CustomerMapper::toResponse)
                .collect(Collectors.toList());

        long totalItems = customerDAO.countCustomers(emailFilter);

        return new PaginatedResponse<>(dtoList, totalItems, page, size);
    }

    // -------------------------
    // GET CUSTOMER BY ID
    // -------------------------
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerDAO.findById(Customer.class, id);
        if (customer == null) return null;

        enforceAccess(customer.getId());
        return CustomerMapper.toResponse(customer);
    }

    // -------------------------
    // CREATE CUSTOMER
    // -------------------------
    public CustomerResponse createCustomer(CustomerRequest request) {
        // Customers are allowed to self-create
        Customer customer = CustomerMapper.toEntity(request);
        customerDAO.save(customer);
        customerDAO.getEntityManager().flush();

        audit("CREATE_CUSTOMER", "Customer", customer.getId(),
                String.format("{\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}",
                        request.getEmail(), request.getFirstName(), request.getLastName()));

        return CustomerMapper.toResponse(customer);
    }

    // -------------------------
    // UPDATE CUSTOMER
    // -------------------------
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer existing = customerDAO.findById(Customer.class, id);
        if (existing == null) return null;

        enforceAccess(existing.getId());

        CustomerMapper.updateEntity(existing, request);
        customerDAO.update(existing);
        customerDAO.getEntityManager().flush();

        audit("UPDATE_CUSTOMER", "Customer", existing.getId(),
                String.format("{\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}",
                        request.getEmail(), request.getFirstName(), request.getLastName()));

        return CustomerMapper.toResponse(existing);
    }

    // -------------------------
    // DELETE CUSTOMER
    // -------------------------
    public boolean deleteCustomer(Long id) {
        Customer existing = customerDAO.findById(Customer.class, id);
        if (existing == null) return false;

        enforceAccess(existing.getId());

        customerDAO.delete(existing);
        customerDAO.getEntityManager().flush();

        audit("DELETE_CUSTOMER", "Customer", id, "{}");

        return true;
    }

    // -------------------------
    // OWNERSHIP / ROLE CHECKS
    // -------------------------
    private void enforceAccess(Long customerId) {
        if (jwtService.isAdmin()) return;

        if (jwtService.isCustomer()) {
            Long currentUserId = jwtService.getCurrentUserId();
            if (!currentUserId.equals(customerId)) {
                throw new SecurityException("Forbidden: Cannot access another customer's data");
            }
        } else {
            throw new SecurityException("Forbidden: Unknown role");
        }
    }

    private void requireAdmin(String message) {
        if (!jwtService.isAdmin()) {
            throw new SecurityException("Forbidden: " + message);
        }
    }

    // -------------------------
    // AUDIT WRAPPER
    // -------------------------
    private void audit(String action, String entityType, Long entityId, String payload) {
        String actor = jwtService.getUsername();
        auditService.record(actor, action, entityType, entityId, payload);
    }
}
