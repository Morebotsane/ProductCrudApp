package com.example.services;

import com.example.dto.AddressRequest;
import com.example.dto.AddressResponse;
import com.example.dto.mappers.CustomerAddressMapper;
import com.example.entities.Address;
import com.example.entities.Customer;
import com.example.dao.AddressDAO;
import com.example.dao.CustomerDAO;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class CustomerAddressService {

    @Inject
    private AddressDAO addressDAO;

    @Inject
    private CustomerDAO customerDAO;

    @Inject
    private CustomerAddressMapper mapper;

    /** Add a new address for a customer */
    @Transactional
    public AddressResponse addAddress(Long customerId, AddressRequest request) {
        Customer customer = customerDAO.findById(Customer.class, customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        Address address = mapper.toEntity(request);
        address.setCustomer(customer);

        // If marked as default, reset other addresses
        if (address.isDefault()) {
            addressDAO.findByCustomer(customer).forEach(a -> a.setDefault(false));
        }

        addressDAO.save(address);
        return mapper.toResponse(address);
    }

    /** Get all addresses for a customer */
    public List<AddressResponse> getAddresses(Long customerId) {
        Customer customer = customerDAO.findById(Customer.class, customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        return addressDAO.findByCustomer(customer).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /** Update an existing address for a customer */
    @Transactional
    public AddressResponse updateAddress(Long customerId, Long addressId, AddressRequest request) {
        Customer customer = customerDAO.findById(Customer.class, customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        Address address = addressDAO.findById(Address.class, addressId);
        if (address == null || !address.getCustomer().getId().equals(customerId)) {
            return null; // Not found or doesn't belong to this customer
        }

        mapper.updateEntity(address, request);

        // Handle default flag
        if (request.isDefault()) {
            addressDAO.findByCustomer(customer).forEach(a -> {
                if (!a.getId().equals(addressId)) {
                    a.setDefault(false);
                }
            });
            address.setDefault(true);
        }

        addressDAO.update(address);
        return mapper.toResponse(address);
    }

    /** Delete an address for a customer */
    @Transactional
    public boolean deleteAddress(Long customerId, Long addressId) {
        Customer customer = customerDAO.findById(Customer.class, customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        Address address = addressDAO.findById(Address.class, addressId);
        if (address == null || !address.getCustomer().getId().equals(customerId)) {
            return false;
        }

        addressDAO.delete(address);
        return true;
    }
}

