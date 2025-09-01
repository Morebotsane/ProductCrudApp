package com.example.dao;

import com.example.entities.Address;
import com.example.entities.AddressType;
import com.example.entities.Customer;
import jakarta.ejb.Stateless;

import java.util.List;
import java.util.Optional;

@Stateless
public class AddressDAO extends BaseDAO<Address> {

    public AddressDAO() {
        super(Address.class);
    }

    /** Find all addresses belonging to a specific customer */
    public List<Address> findByCustomer(Customer customer) {
        return em.createQuery(
                "SELECT a FROM Address a WHERE a.customer = :customer", Address.class)
                .setParameter("customer", customer)
                .getResultList();
    }

    /** Find the default *shipping* address for a specific customer */
    public Optional<Address> findDefaultShippingByCustomer(Customer customer) {
        return em.createQuery(
                "SELECT a FROM Address a " +
                "WHERE a.customer = :customer " +
                "AND a.isDefault = true " +
                "AND a.type = :type", Address.class)
            .setParameter("customer", customer)
            .setParameter("type", AddressType.SHIPPING)
            .setMaxResults(1)
            .getResultStream()
            .findFirst();
    }
}


