package com.example.dao;

import com.example.entities.Cart;
import com.example.entities.CartStatus;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class CartDAO extends BaseDAO<Cart> {

    public CartDAO() {
        super(Cart.class);
    }

    /**
     * Find the currently active (NEW) cart for a given customer.
     * Returns null if none found.
     */
    public Cart findActiveCartByCustomerId(Long customerId) {
        TypedQuery<Cart> query = em.createQuery(
            "SELECT c FROM Cart c WHERE c.customer.id = :customerId AND c.status = :status",
            Cart.class
        );
        query.setParameter("customerId", customerId);
        query.setParameter("status", CartStatus.NEW);

        List<Cart> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Find all carts by status that expired before given time.
     */
    public List<Cart> findByStatusAndExpiresBefore(CartStatus status, LocalDateTime time) {
        TypedQuery<Cart> query = em.createQuery(
            "SELECT c FROM Cart c WHERE c.status = :status AND c.expiresAt < :time",
            Cart.class
        );
        query.setParameter("status", status);
        query.setParameter("time", time);

        return query.getResultList();
    }

    /**
     * Bulk update helper (merge all entities in a list).
     */
    public void updateAll(List<Cart> carts) {
        for (Cart cart : carts) {
            em.merge(cart);
        }
    }
}

