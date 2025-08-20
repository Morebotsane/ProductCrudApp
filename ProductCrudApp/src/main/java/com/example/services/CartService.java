package com.example.services;

import com.example.entities.Cart;
import com.example.entities.CartItem;
import com.example.entities.Customer;
import com.example.entities.Product;
import com.example.dao.CartDAO;
import com.example.dao.CustomerDAO;
import com.example.dao.ProductDAO;
//import com.example.dto.CartItemResponse;
//import com.example.dto.CartResponse;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.math.BigDecimal;
//import java.util.List;
import java.util.Optional;

@Stateless
public class CartService {

    @Inject
    private CartDAO cartDAO;

    @Inject
    private ProductDAO productDAO;

    @Inject
    private CustomerDAO customerDAO;

    private static final BigDecimal VAT_RATE = new BigDecimal("0.15");

    // ---- Core business methods ----

    public Cart createCartForCustomer(Long customerId) {
        Customer customer = customerDAO.findById(Customer.class, customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setStatus("NEW");
        cartDAO.save(cart);
        return cart;
    }

    public Cart addProduct(Long cartId, Long productId, int quantity) {
        Cart cart = getCartById(cartId);
        Product product = productDAO.findById(Product.class, productId);

        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem(product, quantity);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        cartDAO.update(cart);
        return cart;
    }

    public Cart removeProduct(Long cartId, Long productId) {
        Cart cart = getCartById(cartId);

        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        if (!removed) {
            throw new IllegalArgumentException("Product not found in cart");
        }

        cartDAO.update(cart);
        return cart;
    }

    public Cart decrementProductQuantity(Long cartId, Long productId) {
        Cart cart = getCartById(cartId);

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            int currentQuantity = item.getQuantity();

            if (currentQuantity > 1) {
                item.setQuantity(currentQuantity - 1);
            } else {
                cart.getItems().remove(item);
            }
            cartDAO.update(cart);
        } else {
            throw new IllegalArgumentException("Product not found in cart");
        }

        return cart;
    }

    // ---- Calculation logic (kept in service layer) ----

    public BigDecimal getTotal(Cart cart) {
        return cart.getItems().stream()
                   .map(CartItem::getSubtotal)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalWithVAT(Cart cart) {
        BigDecimal total = getTotal(cart);
        return total.add(total.multiply(VAT_RATE));
    }

    public BigDecimal getTotal(Long cartId) {
        return getTotal(getCartById(cartId));
    }

    public BigDecimal getTotalWithVAT(Long cartId) {
        return getTotalWithVAT(getCartById(cartId));
    }

    // ---- DAO Access ----

    public Cart getCartById(Long cartId) {
        Cart cart = cartDAO.findById(Cart.class, cartId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found");
        }
        return cart;
    }
}