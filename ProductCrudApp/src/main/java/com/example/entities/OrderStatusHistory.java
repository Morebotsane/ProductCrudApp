package com.example.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history") 
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)// The initial status of the cart on creation is null
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus toStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;

    public OrderStatusHistory() {
        this.changedAt = LocalDateTime.now();
    }
    
    public OrderStatusHistory(Order order, OrderStatus toStatus) {
        this.order = order;
        this.fromStatus = null;
        this.toStatus = toStatus;
        this.changedAt = LocalDateTime.now();
    }

    public OrderStatusHistory(Order order, OrderStatus fromStatus, OrderStatus toStatus) {
        this.order = order;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = LocalDateTime.now();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public OrderStatus getFromStatus() {
		return fromStatus;
	}

	public void setFromStatus(OrderStatus fromStatus) {
		this.fromStatus = fromStatus;
	}

	public OrderStatus getToStatus() {
		return toStatus;
	}

	public void setToStatus(OrderStatus toStatus) {
		this.toStatus = toStatus;
	}

	public LocalDateTime getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(LocalDateTime changedAt) {
		this.changedAt = changedAt;
	}
}
