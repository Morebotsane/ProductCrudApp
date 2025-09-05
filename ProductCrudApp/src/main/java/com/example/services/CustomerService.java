package com.example.services;

import com.example.dao.CustomerDAO;
import com.example.dto.CustomerRequest;
import com.example.dto.CustomerResponse;
import com.example.dto.PaginatedResponse;
import com.example.dto.mappers.CustomerMapper;
import com.example.entities.Customer;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class CustomerService {

    @Inject
    private CustomerDAO customerDAO;

    @Inject
    private AuditService auditService;

    /** List customers with optional filtering and pagination */
    public PaginatedResponse<CustomerResponse> getCustomers(int page, int size, String emailFilter) {
        int offset = (page - 1) * size;

        List<Customer> customers = customerDAO.findCustomers(offset, size, emailFilter);

        List<CustomerResponse> dtoList = customers.stream()
                .map(CustomerMapper::toResponse)
                .collect(Collectors.toList());

        long totalItems = customerDAO.countCustomers(emailFilter);

        return new PaginatedResponse<>(dtoList, totalItems, page, size);
    }

    /** Retrieve a single customer */
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerDAO.findById(Customer.class, id);
        return (customer != null) ? CustomerMapper.toResponse(customer) : null;
    }

    /** Create a new customer and record audit */
    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = CustomerMapper.toEntity(request);
        customerDAO.save(customer);
        customerDAO.getEntityManager().flush(); // ensure ID is generated

        auditService.record(
                "system",
                "CREATE_CUSTOMER",
                "Customer",
                customer.getId(),
                String.format("{\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}",
                        request.getEmail(), request.getFirstName(), request.getLastName())
        );

        return CustomerMapper.toResponse(customer);
    }

    /** Update existing customer and record audit */
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer existing = customerDAO.findById(Customer.class, id);
        if (existing == null) return null;

        CustomerMapper.updateEntity(existing, request);
        customerDAO.update(existing);
        customerDAO.getEntityManager().flush();

        auditService.record(
                "system",
                "UPDATE_CUSTOMER",
                "Customer",
                existing.getId(),
                String.format("{\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}",
                        request.getEmail(), request.getFirstName(), request.getLastName())
        );

        return CustomerMapper.toResponse(existing);
    }

    /** Delete a customer and record audit */
    public boolean deleteCustomer(Long id) {
        Customer existing = customerDAO.findById(Customer.class, id);
        if (existing == null) return false;

        customerDAO.delete(existing);
        customerDAO.getEntityManager().flush();

        auditService.record(
                "system",
                "DELETE_CUSTOMER",
                "Customer",
                id,
                "{}"
        );

        return true;
    }
}
