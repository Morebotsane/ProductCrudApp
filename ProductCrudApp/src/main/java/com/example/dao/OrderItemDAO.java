package com.example.dao;

import com.example.entities.OrderItem;
import jakarta.ejb.Stateless;

@Stateless
public class OrderItemDAO extends BaseDAO<OrderItem> {
    public OrderItemDAO() {
        super(OrderItem.class);
    }
}

