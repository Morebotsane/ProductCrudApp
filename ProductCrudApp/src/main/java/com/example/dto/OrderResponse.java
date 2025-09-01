package com.example.dto;

import com.example.entities.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long id;
    private Long customerId;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private BigDecimal subtotal;
    private BigDecimal vatTotal;
    private BigDecimal total;
    private List<OrderItemResponse> items;
    private AddressSnapshotResponse shippingAddress;
    private List<PaymentDTO> payments;
    private List<OrderStatusHistoryDTO> history;

    // --- Full constructor (needed for mapOrderToResponse) ---
    public OrderResponse(Long id, Long customerId, OrderStatus status, LocalDateTime orderDate,
                         BigDecimal subtotal, BigDecimal vatTotal, BigDecimal total,
                         List<OrderItemResponse> items, AddressSnapshotResponse shippingAddress,
                         List<PaymentDTO> payments, List<OrderStatusHistoryDTO> history) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.orderDate = orderDate;
        this.subtotal = subtotal;
        this.vatTotal = vatTotal;
        this.total = total;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.payments = payments;
        this.history = history;
    }

    // --- No-arg constructor (optional) ---
    public OrderResponse() {}

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getVatTotal() { return vatTotal; }
    public void setVatTotal(BigDecimal vatTotal) { this.vatTotal = vatTotal; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }

    public AddressSnapshotResponse getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(AddressSnapshotResponse shippingAddress) { this.shippingAddress = shippingAddress; }

    public List<PaymentDTO> getPayments() { return payments; }
    public void setPayments(List<PaymentDTO> payments) { this.payments = payments; }

    public List<OrderStatusHistoryDTO> getHistory() { return history; }
    public void setHistory(List<OrderStatusHistoryDTO> history) { this.history = history; }
}
