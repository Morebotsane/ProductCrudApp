package com.example.dao;

import com.example.entities.Order;
import com.example.entities.OrderStatusHistory;

import jakarta.ejb.Stateless;
import java.util.List;

@Stateless
public class OrderStatusHistoryDAO extends BaseDAO<OrderStatusHistory> {

    public OrderStatusHistoryDAO() {
        super(OrderStatusHistory.class);
    }

    public List<OrderStatusHistory> findByOrder(Order order) {
        return em.createQuery(
                "SELECT h FROM OrderStatusHistory h WHERE h.order = :order ORDER BY h.changedAt ASC",
                OrderStatusHistory.class)
            .setParameter("order", order)
            .getResultList();
    }

    public List<OrderStatusHistory> findByOrderId(Long orderId) {
        return em.createQuery(
                "SELECT h FROM OrderStatusHistory h WHERE h.order.id = :orderId ORDER BY h.changedAt ASC",
                OrderStatusHistory.class)
            .setParameter("orderId", orderId)
            .getResultList();
    }
}