package com.example.services;

import com.example.entities.Cart;
import com.example.entities.CartItem;
import com.example.entities.CartStatus;
import com.example.entities.Customer;
import com.example.entities.Product;
import com.example.dao.CartDAO;
import com.example.dao.CustomerDAO;
import com.example.dao.ProductDAO;

import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Stateless
public class CartService {

    @Inject
    private CartDAO cartDAO;

    @Inject
    private ProductDAO productDAO;

    @Inject
    private CustomerDAO customerDAO;

    @Inject
    private AuditService auditService;

    private static final BigDecimal VAT_RATE = new BigDecimal("0.15");

    // -------------------------
    // Active cart helpers
    // -------------------------

    @Transactional
    public Cart getOrCreateActiveCart(Long customerId) {
        Customer customer = customerDAO.findById(Customer.class, customerId);
        if (customer == null) throw new IllegalArgumentException("Customer not found");

        Cart activeCart = cartDAO.findActiveCartByCustomerId(customerId);
        if (activeCart != null) return activeCart;

        Cart newCart = new Cart();
        newCart.setCustomer(customer);
        newCart.setStatus(CartStatus.NEW);
        LocalDateTime now = LocalDateTime.now();
        newCart.setCreatedAt(now);
        newCart.setUpdatedAt(now);
        newCart.setExpiresAt(now.plusHours(2));

        cartDAO.save(newCart);
        cartDAO.getEntityManager().flush();

        auditService.record(
                "system",
                "CREATE_CART",
                "Cart",
                newCart.getId(),
                String.format("{\"customerId\": %d}", customerId)
        );

        return newCart;
    }

    // -------------------------
    // Add product idempotently
    // -------------------------

    @Transactional
    public Cart addProduct(Long cartId, Long productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");

        Cart cart = getCartById(cartId);
        Product product = productDAO.findById(Product.class, productId);
        if (product == null) throw new IllegalArgumentException("Product not found");

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > product.getStock())
                throw new IllegalArgumentException("Not enough stock for " + product.getName());
            item.setQuantity(newQuantity);
        } else {
            if (quantity > product.getStock())
                throw new IllegalArgumentException("Not enough stock for " + product.getName());

            CartItem newItem = new CartItem(product, quantity);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartDAO.update(cart);
        cartDAO.getEntityManager().flush();

        auditService.record(
                "system",
                "ADD_PRODUCT_TO_CART",
                "Cart",
                cartId,
                String.format("{\"productId\": %d, \"quantity\": %d}", productId, quantity)
        );

        return cart;
    }

    // -------------------------
    // Remove / decrement / clear
    // -------------------------

    @Transactional
    public Cart removeProduct(Long cartId, Long productId) {
        Cart cart = getCartById(cartId);
        boolean removed = cart.getItems().removeIf(ci -> ci.getProduct().getId().equals(productId));
        if (!removed) throw new IllegalArgumentException("Product not found in cart");

        cart.setUpdatedAt(LocalDateTime.now());
        cartDAO.update(cart);
        cartDAO.getEntityManager().flush();

        auditService.record(
                "system",
                "REMOVE_PRODUCT_FROM_CART",
                "Cart",
                cartId,
                String.format("{\"productId\": %d}", productId)
        );

        return cart;
    }

    @Transactional
    public Cart decrementProductQuantity(Long cartId, Long productId) {
        Cart cart = getCartById(cartId);

        cart.getItems().stream()
            .filter(ci -> ci.getProduct().getId().equals(productId))
            .findFirst()
            .ifPresentOrElse(item -> {
                if (item.getQuantity() > 1) item.setQuantity(item.getQuantity() - 1);
                else cart.getItems().remove(item);
                cart.setUpdatedAt(LocalDateTime.now());
                cartDAO.update(cart);
                cartDAO.getEntityManager().flush();

                auditService.record(
                        "system",
                        "DECREMENT_PRODUCT_IN_CART",
                        "Cart",
                        cartId,
                        String.format("{\"productId\": %d}", productId)
                );
            }, () -> { throw new IllegalArgumentException("Product not found in cart"); });

        return cart;
    }

    @Transactional
    public Cart clearCart(Long cartId) {
        Cart cart = getCartById(cartId);
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartDAO.update(cart);
        cartDAO.getEntityManager().flush();

        auditService.record(
                "system",
                "CLEAR_CART",
                "Cart",
                cartId,
                "{}"
        );

        return cart;
    }

    // -------------------------
    // Totals
    // -------------------------

    public BigDecimal getTotal(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalWithVAT(Cart cart) {
        BigDecimal total = getTotal(cart);
        return total.add(total.multiply(VAT_RATE));
    }

    // -------------------------
    // Expiry handling (scheduled)
    // -------------------------

    @Schedule(minute = "*/5", hour = "*", persistent = false)
    @Transactional
    public void expireCarts() {
        List<Cart> expired = cartDAO.findByStatusAndExpiresBefore(CartStatus.NEW, LocalDateTime.now());
        if (expired.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        for (Cart cart : expired) {
            cart.setStatus(CartStatus.EXPIRED);
            cart.setUpdatedAt(now);

            auditService.record(
                    "system",
                    "EXPIRE_CART",
                    "Cart",
                    cart.getId(),
                    "{}"
            );
        }
        cartDAO.updateAll(expired);
        cartDAO.getEntityManager().flush();
    }

    // -------------------------
    // DAO access helpers
    // -------------------------

    public Cart getCartById(Long cartId) {
        Cart cart = cartDAO.findById(Cart.class, cartId);
        if (cart == null) throw new IllegalArgumentException("Cart not found");
        return cart;
    }
}