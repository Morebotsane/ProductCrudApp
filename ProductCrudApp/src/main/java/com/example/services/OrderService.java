package com.example.services;

import com.example.dao.CartDAO;
import com.example.dao.OrderDAO;
import com.example.dto.OrderResponse;
import com.example.entities.Cart;
import com.example.entities.CartItem;
import com.example.entities.Order;
import com.example.entities.OrderItem;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class OrderService {

    @Inject
    private OrderDAO orderDAO;

    @Inject
    private CartDAO cartDAO;

    private static final BigDecimal VAT_RATE = new BigDecimal("0.15");

    /** Create order from a cart and return as DTO */
    public OrderResponse createOrderFromCartDto(Long cartId) {
        Cart cart = cartDAO.findById(Cart.class, cartId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found");
        }
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order from empty cart");
        }
        if ("CHECKED_OUT".equalsIgnoreCase(cart.getStatus())) {
            throw new IllegalStateException("Cart has already been checked out");
        }

        BigDecimal totalWithoutVAT = cart.getItems().stream()
                .map(ci -> ci.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithVAT = totalWithoutVAT.add(totalWithoutVAT.multiply(VAT_RATE));

        Order order = new Order();
        order.setCart(cart);
        order.setCustomer(cart.getCustomer());
        order.setStatus("NEW");
        order.setOrderDate(LocalDateTime.now());
        order.setTotal(totalWithVAT);

        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getProduct().getPrice());
            order.addItem(oi);
        }

        orderDAO.save(order);

        // Update cart status
        cart.setStatus("CHECKED_OUT");
        cartDAO.update(cart);

        return OrderResponse.fromEntity(order);
    }

    /** Update order status and return as DTO */
    public OrderResponse updateStatusDto(Long orderId, String status) {
        Order order = orderDAO.findById(Order.class, orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        order.setStatus(status);
        orderDAO.update(order);
        return OrderResponse.fromEntity(order);
    }

    /** Get single order as DTO */
    public OrderResponse getOrderDto(Long orderId) {
        Order order = orderDAO.findById(Order.class, orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        return OrderResponse.fromEntity(order);
    }

    /** Get all orders as DTOs */
    public List<OrderResponse> getAllOrderDtos() {
        return orderDAO.findAll(Order.class).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** Get all orders for a specific customer as DTOs */
    public List<OrderResponse> getOrdersByCustomerDto(Long customerId) {
        return orderDAO.findAll(Order.class).stream()
                .filter(o -> o.getCustomer() != null
                        && o.getCustomer().getId().equals(customerId))
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }
}



