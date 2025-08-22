package com.example.services;

import com.example.dao.CustomerDAO;
import com.example.dto.mappers.CustomerMapper;
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

    public PaginatedResponse<CustomerResponse> getCustomers(int page, int size, String emailFilter) {
        int offset = (page - 1) * size;
        List<Customer> customers = customerDAO.findCustomers(offset, size, emailFilter);

        List<CustomerResponse> dtoList = customers.stream()
                .map(CustomerMapper::toResponse)
                .collect(Collectors.toList());

        long totalItems = customerDAO.countCustomers(emailFilter);
        return new PaginatedResponse<>(dtoList, totalItems, page, size);
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerDAO.findById(Customer.class, id);
        return CustomerMapper.toResponse(customer);
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = CustomerMapper.toEntity(request);
        customerDAO.save(customer);
        return CustomerMapper.toResponse(customer);
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer existing = customerDAO.findById(Customer.class, id);
        if (existing == null) return null;

        CustomerMapper.updateEntity(existing, request);
        customerDAO.update(existing);

        return CustomerMapper.toResponse(existing);
    }

    public boolean deleteCustomer(Long id) {
        Customer existing = customerDAO.findById(Customer.class, id);
        if (existing != null) {
            customerDAO.delete(existing);
            return true;
        }
        return false;
    }
}

