package com.example.entities;

import java.util.*;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;

@Entity
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String status = "NEW";

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonbTransient
    private List<CartItem> items = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public Cart() {}

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }
    
 // setter for testing only this was done for unit testing
    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}  
}




