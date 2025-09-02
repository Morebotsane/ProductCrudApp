package com.example.services;

import com.example.dao.OrderDAO;
import com.example.dao.PaymentDAO;
import com.example.dao.OrderStatusHistoryDAO;
import com.example.dto.OrderResponse;
import com.example.dto.mappers.OrderMapper;
import com.example.dto.mappers.PaymentMapper;
import com.example.entities.*;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class PaymentService {

    @Inject private OrderDAO orderDAO;
    @Inject private PaymentDAO paymentDAO;
    @Inject private OrderStatusHistoryDAO orderStatusHistoryDAO;

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

    public List<Payment> getPaymentsForOrder(Order order) {
        return paymentDAO.findByOrder(order);
    }

    private void logStatusChange(Order order, OrderStatus from, OrderStatus to) {
        orderStatusHistoryDAO.save(new OrderStatusHistory(order, from, to));
    }

    private OrderResponse mapOrderToResponse(Order order) {
        OrderResponse dto = OrderMapper.toDto(order);
        dto.setPayments(PaymentMapper.toDto(paymentDAO.findByOrder(order)));
        return dto;
    }
}
