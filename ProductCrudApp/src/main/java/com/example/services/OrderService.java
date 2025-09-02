package com.example.services;

import com.example.dao.*;
import com.example.dto.OrderResponse;
import com.example.dto.mappers.OrderMapper;
import com.example.dto.mappers.PaymentMapper;
import com.example.dto.mappers.OrderStatusHistoryMapper;
import com.example.entities.*;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class OrderService {

    @Inject private OrderDAO orderDAO;
    @Inject private CartDAO cartDAO;
    @Inject private ProductDAO productDAO;
    @Inject private AddressDAO addressDAO;
    @Inject private OrderStatusHistoryDAO orderStatusHistoryDAO;
    @Inject private PaymentDAO paymentDAO;

    private static final BigDecimal VAT_RATE = new BigDecimal("0.15");

    // -------------------------
    // CREATE ORDER FROM CART
    // -------------------------
    @Transactional
    public OrderResponse createOrderFromCartDto(Long cartId) {
        Cart cart = cartDAO.findById(Cart.class, cartId);
        if (cart == null) throw new IllegalArgumentException("Cart not found");
        if (cart.getItems() == null || cart.getItems().isEmpty())
            throw new IllegalArgumentException("Cannot create order from empty cart");
        if (cart.getStatus() == CartStatus.CHECKED_OUT)
            throw new IllegalStateException("Cart already checked out");

        Customer customer = cart.getCustomer();
        Address address = addressDAO.findDefaultShippingByCustomer(customer)
                .orElseThrow(() -> new IllegalArgumentException("Customer has no default shipping address"));

        Order order = new Order();
        order.setCart(cart);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.NEW);
        order.setOrderDate(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            Product product = productDAO.findById(Product.class, ci.getProduct().getId());
            if (product == null) throw new IllegalArgumentException("Product not found: " + ci.getProduct().getId());
            if (product.getStock() < ci.getQuantity())
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());

            product.setStock(product.getStock() - ci.getQuantity());
            productDAO.update(product);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(ci.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setOrder(order);
            order.addItem(item);

            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        BigDecimal vatTotal = subtotal.multiply(VAT_RATE);
        BigDecimal total = subtotal.add(vatTotal);

        order.setSubtotal(subtotal);
        order.setVatTotal(vatTotal);
        order.setTotal(total);

        AddressSnapshot snapshot = new AddressSnapshot();
        snapshot.setLine1(address.getLine1());
        snapshot.setLine2(address.getLine2());
        snapshot.setCity(address.getCity());
        snapshot.setRegion(address.getRegion());
        snapshot.setPostalCode(address.getPostalCode());
        snapshot.setCountry(address.getCountry());
        order.setShippingAddress(snapshot);

        orderDAO.save(order);
        logStatusChange(order, null, OrderStatus.NEW);

        cart.setStatus(CartStatus.CHECKED_OUT);
        cartDAO.update(cart);

        return mapOrderToResponse(order);
    }

    // -------------------------
    // PAY ORDER
    // -------------------------
    @Transactional
    public OrderResponse payOrder(Long orderId, BigDecimal amount, PaymentMethod method, String txnRef) {
        Order order = orderDAO.findById(Order.class, orderId);
        if (order == null) throw new IllegalArgumentException("Order not found");

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setTxnRef(txnRef);
        payment.setCreatedAt(LocalDateTime.now());

        if (amount.compareTo(order.getTotal()) >= 0) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            paymentDAO.save(payment);

            OrderStatus oldStatus = order.getStatus();
            order.setStatus(OrderStatus.PAID);
            orderDAO.update(order);
            logStatusChange(order, oldStatus, OrderStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentDAO.save(payment);
        }

        return mapOrderToResponse(order);
    }

    // -------------------------
    // UPDATE ORDER STATUS
    // -------------------------
    @Transactional
    public OrderResponse updateStatusDto(Long orderId, OrderStatus newStatus) {
        Order order = orderDAO.findById(Order.class, orderId);
        if (order == null) throw new IllegalArgumentException("Order not found");
        if (!isValidTransition(order.getStatus(), newStatus))
            throw new IllegalStateException("Invalid transition: " + order.getStatus() + " -> " + newStatus);

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        orderDAO.update(order);
        logStatusChange(order, oldStatus, newStatus);

        return mapOrderToResponse(order);
    }

    // -------------------------
    // QUERY METHODS (for OrderResource)
    // -------------------------
    public OrderResponse getOrderDto(Long orderId) {
        Order order = orderDAO.findById(Order.class, orderId);
        if (order == null) throw new IllegalArgumentException("Order not found");
        return mapOrderToResponse(order);
    }

    public List<OrderResponse> getAllOrderDtos() {
        return orderDAO.findAll(Order.class).stream()
                .map(this::mapOrderToResponse)
                .toList();
    }

    public List<OrderResponse> getOrdersByCustomerDto(Long customerId) {
        return orderDAO.findAll(Order.class).stream()
                .filter(o -> o.getCustomer() != null && o.getCustomer().getId().equals(customerId))
                .map(this::mapOrderToResponse)
                .toList();
    }

    // -------------------------
    // PRIVATE HELPERS
    // -------------------------
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case NEW -> to == OrderStatus.PAID || to == OrderStatus.CANCELLED;
            case PAID -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED -> to == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    private void logStatusChange(Order order, OrderStatus from, OrderStatus to) {
        orderStatusHistoryDAO.save(new OrderStatusHistory(order, from, to));
    }

    private OrderResponse mapOrderToResponse(Order order) {
        OrderResponse dto = OrderMapper.toDto(order);
        dto.setPayments(PaymentMapper.toDto(paymentDAO.findByOrder(order)));
        dto.setHistory(OrderStatusHistoryMapper.toDto(orderStatusHistoryDAO.findByOrder(order)));
        return dto;
    }
}
