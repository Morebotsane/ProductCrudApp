package com.example.dao;

import com.example.entities.Customer;
import jakarta.ejb.Stateless;
//import jakarta.enterprise.context.ApplicationScoped;

@Stateless
public class CustomerDAO extends BaseDAO<Customer> {
    public CustomerDAO() {
        super(Customer.class);
    }
}
