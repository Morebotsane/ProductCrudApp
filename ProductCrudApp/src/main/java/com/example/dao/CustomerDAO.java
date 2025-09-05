package com.example.dao;

import com.example.entities.Customer;
import jakarta.ejb.Stateless;
import java.util.List;

@Stateless
public class CustomerDAO extends BaseDAO<Customer> {

    public CustomerDAO() {
        super(Customer.class);
    }

    public List<Customer> findCustomers(int offset, int limit, String emailFilter) {
        String jpql = "SELECT c FROM Customer c";
        if (emailFilter != null && !emailFilter.isEmpty()) {
            jpql += " WHERE LOWER(c.email) LIKE LOWER(:emailFilter)";
        }

        var query = getEntityManager().createQuery(jpql, Customer.class);

        if (emailFilter != null && !emailFilter.isEmpty()) {
            query.setParameter("emailFilter", "%" + emailFilter + "%");
        }

        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    public long countCustomers(String emailFilter) {
        String jpql = "SELECT COUNT(c) FROM Customer c";
        if (emailFilter != null && !emailFilter.isEmpty()) {
            jpql += " WHERE LOWER(c.email) LIKE LOWER(:emailFilter)";
        }

        var query = getEntityManager().createQuery(jpql, Long.class);

        if (emailFilter != null && !emailFilter.isEmpty()) {
            query.setParameter("emailFilter", "%" + emailFilter + "%");
        }

        return query.getSingleResult();
    }
}