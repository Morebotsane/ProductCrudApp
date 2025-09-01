package com.example.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"order\"") // quoting is safest
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    //private String status; // e.g. NEW, PAID, SHIPPED

    private LocalDateTime orderDate;

    @Column(precision = 19, scale = 4)
    private BigDecimal total;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(precision = 19, scale = 4)
    private BigDecimal vatTotal;
    
    @Embedded
    private AddressSnapshot shippingAddress;
    
    private LocalDateTime createdAt;
    
    public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Order(){
    	this.orderDate = LocalDateTime.now();
    	this.status = OrderStatus.NEW;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public Long getId() {
        return id;
    }

    //this was done for unit testing
    public void setId(Long id) {
        this.id = id;
    }

    public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getVatTotal() {
		return vatTotal;
	}

	public void setVatTotal(BigDecimal vatTotal) {
		this.vatTotal = vatTotal;
	}

	public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public void setCustomer(Customer customer) {
    	this.customer = customer;
    }
    
    public Customer getCustomer() {
    	return customer;
    }
    
    public AddressSnapshot getShippingAddress() { 
    	return shippingAddress; 
    }
    
    public void setShippingAddress(AddressSnapshot shippingAddress) { 
    	this.shippingAddress = shippingAddress; 
    }
}