package com.example.dao;

import com.example.entities.Customer;
import com.example.entities.Order;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class OrderDAO extends BaseDAO<Order> {

    public OrderDAO() {
        super(Order.class);
    }

    // Overloaded version: simple findAll (no pagination/filter)
    public List<Order> findAll() {
        return em.createQuery("SELECT o FROM Order o", Order.class)
                 .getResultList();
    }

    // Your existing paginated + filtered version
    public List<Order> findAll(int page, int size, String statusFilter) {
        String jpql = "SELECT o FROM Order o";
        if (statusFilter != null && !statusFilter.isEmpty()) {
            jpql += " WHERE o.status = :status";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class);

        if (statusFilter != null && !statusFilter.isEmpty()) {
            query.setParameter("status", statusFilter);
        }

        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList();
    }

    // New method: countAll (already good)
    public long countAll(String statusFilter) {
        String jpql = "SELECT COUNT(o) FROM Order o";
        if (statusFilter != null && !statusFilter.isEmpty()) {
            jpql += " WHERE o.status = :status";
        }

        TypedQuery<Long> query = em.createQuery(jpql, Long.class);

        if (statusFilter != null && !statusFilter.isEmpty()) {
            query.setParameter("status", statusFilter);
        }

        return query.getSingleResult();
    }

    // New method: find orders by customer
    public List<Order> findByCustomer(Customer customer) {
        return em.createQuery(
                "SELECT o FROM Order o WHERE o.customer = :customer", Order.class)
                .setParameter("customer", customer)
                .getResultList();
    }
}