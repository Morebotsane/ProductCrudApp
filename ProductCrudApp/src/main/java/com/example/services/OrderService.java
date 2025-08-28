package com.example.services;

import com.example.dao.CartDAO;
import com.example.dao.OrderDAO;
import com.example.dao.ProductDAO;
import com.example.dto.OrderResponse;
import com.example.dto.mappers.OrderMapper;
import com.example.entities.Cart;
import com.example.entities.CartItem;
import com.example.entities.Order;
import com.example.entities.OrderItem;
import com.example.entities.Product;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class OrderService {

    @Inject
    private OrderDAO orderDAO;

    @Inject
    private CartDAO cartDAO;

    @Inject
    private ProductDAO productDAO;

    private static final BigDecimal VAT_RATE = new BigDecimal("0.15");

    /** Create an order from a cart and return DTO */
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

        // Create new Order
        Order order = new Order();
        order.setCart(cart);
        order.setCustomer(cart.getCustomer());
        order.setStatus("NEW");
        order.setOrderDate(LocalDateTime.now());

        // Track totals
        BigDecimal totalWithoutVAT = BigDecimal.ZERO;

        // Validate stock & map CartItems → OrderItems
        for (CartItem ci : cart.getItems()) {
            Product product = productDAO.findById(Product.class, ci.getProduct().getId());
            int quantity = ci.getQuantity();

            if (product == null) {
                throw new IllegalArgumentException("Product not found: " + ci.getProduct().getId());
            }
            if (product.getStock() < quantity) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            // Deduct stock
            product.setStock(product.getStock() - quantity);
            productDAO.update(product);

            // Create OrderItem
            OrderItem oi = new OrderItem();
            oi.setProduct(product);
            oi.setQuantity(quantity);
            oi.setUnitPrice(product.getPrice());
            oi.setOrder(order);
            order.addItem(oi);

            totalWithoutVAT = totalWithoutVAT.add(
                    product.getPrice().multiply(BigDecimal.valueOf(quantity))
            );
        }

        // Calculate total including VAT
        BigDecimal totalWithVAT = totalWithoutVAT.add(totalWithoutVAT.multiply(VAT_RATE));
        order.setTotal(totalWithVAT);

        // Persist order
        orderDAO.save(order);

        // Update cart status
        cart.setStatus("CHECKED_OUT");
        cartDAO.update(cart);

        // Return DTO using mapper
        return OrderMapper.toDto(order);
    }

    /** Update order status and return DTO */
    public OrderResponse updateStatusDto(Long orderId, String status) {
        Order order = orderDAO.findById(Order.class, orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        order.setStatus(status);
        orderDAO.update(order);
        return OrderMapper.toDto(order);
    }

    /** Get single order as DTO */
    public OrderResponse getOrderDto(Long orderId) {
        Order order = orderDAO.findById(Order.class, orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        return OrderMapper.toDto(order);
    }

    /** Get all orders as DTOs */
    public List<OrderResponse> getAllOrderDtos() {
        return orderDAO.findAll(Order.class).stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    /** Get all orders for a specific customer as DTOs */
    public List<OrderResponse> getOrdersByCustomerDto(Long customerId) {
        return orderDAO.findAll(Order.class).stream()
                .filter(o -> o.getCustomer() != null && o.getCustomer().getId().equals(customerId))
                .map(OrderMapper::toDto)
                .toList();
    }
}
