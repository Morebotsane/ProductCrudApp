package com.example.dao;

import com.example.entities.Order;
import jakarta.ejb.Stateless;

@Stateless
public class OrderDAO extends BaseDAO<Order> {

    public OrderDAO() {
        super(Order.class);
    }
}

