package com.example.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments") // ✅ explicit table name
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // ✅ lazy to avoid fetching full order unless needed
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) // ✅ constraint enum storage
    private PaymentMethod method;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) // ✅ safer DB column size
    private PaymentStatus status;

    @Column(length = 100, unique = true) // ✅ txnRef should ideally be unique
    private String txnRef;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Payment() {
        this.createdAt = LocalDateTime.now();
    }

    public Payment(Order order, PaymentMethod method, BigDecimal amount, PaymentStatus status, String txnRef) {
        this.order = order;
        this.method = method;
        this.amount = amount;
        this.status = status;
        this.txnRef = txnRef;
        this.createdAt = LocalDateTime.now();
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

	public PaymentMethod getMethod() {
		return method;
	}

	public void setMethod(PaymentMethod method) {
		this.method = method;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public String getTxnRef() {
		return txnRef;
	}

	public void setTxnRef(String txnRef) {
		this.txnRef = txnRef;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

    // --- Getters & Setters (unchanged) ---
}
