package com.example.dao;

import com.example.entities.Order;
import com.example.entities.Payment;

import jakarta.ejb.Stateless;
import java.util.List;

@Stateless
public class PaymentDAO extends BaseDAO<Payment> {

    public PaymentDAO() {
        super(Payment.class);
    }

    /**
     * Find all payments for a given order.
     */
    public List<Payment> findByOrder(Order order) {
        return em.createQuery(
                "SELECT p FROM Payment p WHERE p.order = :order ORDER BY p.createdAt DESC",
                Payment.class)
            .setParameter("order", order)
            .getResultList();
    }

    /**
     * Find all payments for a given orderId.
     */
    public List<Payment> findByOrderId(Long orderId) {
        return em.createQuery(
                "SELECT p FROM Payment p WHERE p.order.id = :orderId ORDER BY p.createdAt DESC",
                Payment.class)
            .setParameter("orderId", orderId)
            .getResultList();
    }

    /**
     * Find all payments for a given customer.
     */
    public List<Payment> findByCustomerId(Long customerId) {
        return em.createQuery(
                "SELECT p FROM Payment p WHERE p.order.customer.id = :customerId ORDER BY p.createdAt DESC",
                Payment.class)
            .setParameter("customerId", customerId)
            .getResultList();
    }
}