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

    @Inject
    private AuditService auditService;

    // -------------------------
    // ADD ADDRESS
    // -------------------------
    @Transactional
    public AddressResponse addAddress(Long customerId, AddressRequest request) {
        Customer customer = getCustomerById(customerId);

        Address address = mapper.toEntity(request);
        address.setCustomer(customer);

        // If marked as default, reset other addresses
        if (address.isDefault()) {
            addressDAO.findByCustomer(customer).forEach(a -> a.setDefault(false));
        }

        addressDAO.save(address);
        addressDAO.getEntityManager().flush();

        auditService.record(
                "system",
                "ADD_ADDRESS",
                "CustomerAddress",
                address.getId(),
                String.format("{\"customerId\": %d, \"addressId\": %d}", customerId, address.getId())
        );

        return mapper.toResponse(address);
    }

    // -------------------------
    // LIST ADDRESSES
    // -------------------------
    public List<AddressResponse> getAddresses(Long customerId) {
        Customer customer = getCustomerById(customerId);

        List<Address> addresses = addressDAO.findByCustomer(customer);
        return addresses.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // -------------------------
    // UPDATE ADDRESS
    // -------------------------
    @Transactional
    public AddressResponse updateAddress(Long customerId, Long addressId, AddressRequest request) {
        Customer customer = getCustomerById(customerId);

        Address address = addressDAO.findById(Address.class, addressId);
        if (address == null || !address.getCustomer().getId().equals(customerId)) {
            return null; // Not found or not owned by this customer
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
        addressDAO.getEntityManager().flush();

        auditService.record(
                "system",
                "UPDATE_ADDRESS",
                "CustomerAddress",
                addressId,
                String.format("{\"customerId\": %d, \"addressId\": %d}", customerId, addressId)
        );

        return mapper.toResponse(address);
    }

    // -------------------------
    // DELETE ADDRESS
    // -------------------------
    @Transactional
    public boolean deleteAddress(Long customerId, Long addressId) {
        //Customer customer = getCustomerById(customerId);

        Address address = addressDAO.findById(Address.class, addressId);
        if (address == null || !address.getCustomer().getId().equals(customerId)) {
            return false;
        }

        addressDAO.delete(address);
        addressDAO.getEntityManager().flush();

        auditService.record(
                "system",
                "DELETE_ADDRESS",
                "CustomerAddress",
                addressId,
                String.format("{\"customerId\": %d, \"addressId\": %d}", customerId, addressId)
        );

        return true;
    }

    // -------------------------
    // DAO helpers
    // -------------------------
    private Customer getCustomerById(Long customerId) {
        Customer customer = customerDAO.findById(Customer.class, customerId);
        if (customer == null) throw new IllegalArgumentException("Customer not found");
        return customer;
    }
}
