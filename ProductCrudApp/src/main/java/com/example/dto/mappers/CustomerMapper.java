package com.example.dto.mappers;

import com.example.dto.CustomerRequest;
import com.example.dto.CustomerResponse;
import com.example.entities.Customer;

public class CustomerMapper {

    public static CustomerResponse toResponse(Customer entity) {
        if (entity == null) return null;
        return new CustomerResponse(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhone()
        );
    }

    public static Customer toEntity(CustomerRequest request) {
        if (request == null) return null;
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        return customer;
    }

    public static void updateEntity(Customer entity, CustomerRequest request) {
        if (request == null || entity == null) return;
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
    }
}
