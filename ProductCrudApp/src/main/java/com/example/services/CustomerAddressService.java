package com.example.services;

import com.example.dto.AddressRequest;
import com.example.dto.AddressResponse;
import com.example.dto.mappers.CustomerAddressMapper;
import com.example.entities.Address;
import com.example.entities.Customer;
import com.example.dao.AddressDAO;
import com.example.dao.CustomerDAO;
import com.example.security.JwtTokenService;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class CustomerAddressService {

    @Inject private AddressDAO addressDAO;
    @Inject private CustomerDAO customerDAO;
    @Inject private CustomerAddressMapper mapper;
    @Inject private AuditService auditService;
    @Inject private JwtTokenService jwtService;

    private static final String ENTITY_TYPE = "CustomerAddress";

    // -------------------------
    // ADD ADDRESS
    // -------------------------
    @Transactional
    public AddressResponse addAddress(Long customerId, AddressRequest request) {
        enforceAccess(customerId);
        Customer customer = getCustomerById(customerId);

        Address address = mapper.toEntity(request);
        address.setCustomer(customer);

        if (address.isDefault()) {
            addressDAO.findByCustomer(customer).forEach(a -> a.setDefault(false));
        }

        addressDAO.save(address);
        addressDAO.getEntityManager().flush();

        audit("ADD_ADDRESS", customerId, address.getId());

        return mapper.toResponse(address);
    }

    // -------------------------
    // LIST ADDRESSES
    // -------------------------
    public List<AddressResponse> getAddresses(Long customerId) {
        enforceAccess(customerId);
        Customer customer = getCustomerById(customerId);

        return addressDAO.findByCustomer(customer).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // -------------------------
    // UPDATE ADDRESS
    // -------------------------
    @Transactional
    public AddressResponse updateAddress(Long customerId, Long addressId, AddressRequest request) {
        enforceAccess(customerId);
        Customer customer = getCustomerById(customerId);

        Address address = addressDAO.findById(Address.class, addressId);
        if (address == null || !address.getCustomer().getId().equals(customerId)) return null;

        mapper.updateEntity(address, request);

        if (request.isDefault()) {
            addressDAO.findByCustomer(customer).forEach(a -> {
                if (!a.getId().equals(addressId)) a.setDefault(false);
            });
            address.setDefault(true);
        }

        addressDAO.update(address);
        addressDAO.getEntityManager().flush();

        audit("UPDATE_ADDRESS", customerId, addressId);

        return mapper.toResponse(address);
    }

    // -------------------------
    // DELETE ADDRESS
    // -------------------------
    @Transactional
    public boolean deleteAddress(Long customerId, Long addressId) {
        enforceAccess(customerId);

        Address address = addressDAO.findById(Address.class, addressId);
        if (address == null || !address.getCustomer().getId().equals(customerId)) return false;

        addressDAO.delete(address);
        addressDAO.getEntityManager().flush();

        audit("DELETE_ADDRESS", customerId, addressId);

        return true;
    }

    // -------------------------
    // DAO helper
    // -------------------------
    private Customer getCustomerById(Long customerId) {
        Customer customer = customerDAO.findById(Customer.class, customerId);
        if (customer == null) throw new IllegalArgumentException("Customer not found");
        return customer;
    }

    // -------------------------
    // Ownership / Role enforcement
    // -------------------------
    private void enforceAccess(Long customerId) {
        if (jwtService.isAdmin()) return;

        if (jwtService.isCustomer()) {
            Long currentUserId = jwtService.getCurrentUserId();
            if (!currentUserId.equals(customerId)) {
                throw new SecurityException("Forbidden: Cannot access another customer's addresses");
            }
        } else {
            throw new SecurityException("Forbidden: Unknown role");
        }
    }

    // -------------------------
    // Audit helper
    // -------------------------
    private void audit(String action, Long customerId, Long addressId) {
        String actor = jwtService.getUsername();
        auditService.record(actor, action, ENTITY_TYPE, addressId,
                String.format("{\"customerId\": %d, \"addressId\": %d}", customerId, addressId));
    }
}
