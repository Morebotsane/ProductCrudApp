package com.example.services;

import com.example.dao.CustomerDAO;
import com.example.dto.CustomerRequest;
import com.example.dto.CustomerResponse;
import com.example.dto.PaginatedResponse;
import com.example.entities.Customer;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class CustomerService {

    @Inject
    private CustomerDAO customerDAO;

    /**
     * Retrieve paginated and optionally filtered customers.
     *
     * @param page        page number (starts at 1)
     * @param size        number of customers per page
     * @param emailFilter optional filter by email
     * @return PaginatedResponse of CustomerResponse
     */
    public PaginatedResponse<CustomerResponse> getCustomers(int page, int size, String emailFilter) {
        int offset = (page - 1) * size;

        // Fetch filtered and paginated customers
        List<Customer> customers = customerDAO.findCustomers(offset, size, emailFilter);

        // Map to DTO
        List<CustomerResponse> dtoList = customers.stream()
                .map(CustomerResponse::fromEntity)
                .collect(Collectors.toList());

        // Count total items for pagination metadata
        long totalItems = customerDAO.countCustomers(emailFilter);

        return new PaginatedResponse<>(dtoList, totalItems, page, size);
    }

    /** Retrieve a single customer by ID */
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerDAO.findById(Customer.class, id);
        if (customer == null) return null;
        return CustomerResponse.fromEntity(customer);
    }

    /** Create a new customer */
    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());

        customerDAO.save(customer);

        return CustomerResponse.fromEntity(customer);
    }

    /** Update existing customer by ID */
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer existing = customerDAO.findById(Customer.class, id);
        if (existing == null) return null;

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setEmail(request.getEmail());

        customerDAO.update(existing);

        return CustomerResponse.fromEntity(existing);
    }

    /** Delete customer by ID */
    public boolean deleteCustomer(Long id) {
        Customer existing = customerDAO.findById(Customer.class, id);
        if (existing != null) {
            customerDAO.delete(existing);
            return true;
        }
        return false;
    }
}
