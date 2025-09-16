package com.example.services;

import com.example.dao.OrderDAO;
import com.example.dao.PaymentDAO;
import com.example.dao.OrderStatusHistoryDAO;
import com.example.dto.OrderResponse;
import com.example.dto.mappers.OrderMapper;
import com.example.dto.mappers.PaymentMapper;
import com.example.entities.*;
import com.example.security.JwtTokenService;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class PaymentService {

    @Inject 
    private OrderDAO orderDAO;

    @Inject 
    private PaymentDAO paymentDAO;

    @Inject 
    private OrderStatusHistoryDAO orderStatusHistoryDAO;

    @Inject
    private JwtTokenService jwtTokenService;

    // -------------------------
    // PAY ORDER
    // -------------------------
    @Transactional
    public OrderResponse payOrder(Long orderId, BigDecimal amount, PaymentMethod method, String txnRef) {
        Order order = orderDAO.findById(Order.class, orderId);
        if (order == null) throw new IllegalArgumentException("Order not found");

        // Enforce that the logged-in customer owns this order (if customer role)
        enforceOrderOwnership(order);

        Payment payment = createPayment(order, amount, method, txnRef);

        if (amount.compareTo(order.getTotal()) >= 0) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            updateOrderStatus(order, OrderStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentDAO.save(payment);
        paymentDAO.getEntityManager().flush(); // ensure persistence

        return mapOrderToResponse(order);
    }

    private Payment createPayment(Order order, BigDecimal amount, PaymentMethod method, String txnRef) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setTxnRef(txnRef);
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    }

    private void updateOrderStatus(Order order, OrderStatus newStatus) {
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        orderDAO.update(order);
        logStatusChange(order, oldStatus, newStatus);
    }

    private void logStatusChange(Order order, OrderStatus from, OrderStatus to) {
        orderStatusHistoryDAO.save(new OrderStatusHistory(order, from, to));
    }

    private OrderResponse mapOrderToResponse(Order order) {
        OrderResponse dto = OrderMapper.toDto(order);
        List<Payment> payments = paymentDAO.findByOrder(order);
        dto.setPayments(PaymentMapper.toDto(payments));
        return dto;
    }

    public List<Payment> getPaymentsForOrder(Order order) {
        return paymentDAO.findByOrder(order);
    }

    // ----------------------------
    // Ownership / Role enforcement
    // ----------------------------
    private void enforceOrderOwnership(Order order) {
        if (jwtTokenService.isCustomer()) {
            Long currentUserId = jwtTokenService.getCurrentUserId();
            if (!order.getCustomer().getId().equals(currentUserId)) {
                throw new SecurityException("Forbidden: Cannot pay for another customer's order");
            }
        }
    }
}
