package com.example.dao;

import com.example.entities.CartItem;
import jakarta.ejb.Stateless;
//import jakarta.enterprise.context.ApplicationScoped;


@Stateless
public class CartItemDAO extends BaseDAO<CartItem> {
    public CartItemDAO() {
        super(CartItem.class);
    }
}
