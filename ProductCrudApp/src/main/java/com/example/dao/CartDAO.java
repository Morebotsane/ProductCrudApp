package com.example.dao;

import com.example.entities.Cart;
import jakarta.ejb.Stateless;
//import jakarta.enterprise.context.ApplicationScoped;


@Stateless
public class CartDAO extends BaseDAO<Cart> {
    public CartDAO() {
        super(Cart.class);
    }
}
