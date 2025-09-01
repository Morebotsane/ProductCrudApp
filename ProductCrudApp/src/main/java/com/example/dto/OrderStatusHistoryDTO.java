package com.example.dto;

import com.example.entities.OrderStatus;

import java.time.LocalDateTime;

public class OrderStatusHistoryDTO {
    private Long id;
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private LocalDateTime changedAt;
    private Long orderId;

    public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public OrderStatusHistoryDTO() {}

    public OrderStatusHistoryDTO(Long id, OrderStatus fromStatus,
                                 OrderStatus toStatus, LocalDateTime changedAt) {
        this.id = id;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = changedAt;
    }

    // --- getters & setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OrderStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(OrderStatus fromStatus) { this.fromStatus = fromStatus; }

    public OrderStatus getToStatus() { return toStatus; }
    public void setToStatus(OrderStatus toStatus) { this.toStatus = toStatus; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}