package com.example.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String firstName;
    
    @NotBlank(message = "Customer name cannot be empty")
    @Column(nullable = false)
    private String lastName;
    
    @Email(message = "Email is required")
    @NotBlank(message = "Email cannot be empty")
    @Column(nullable = false)
    private String email;

    // One customer can have multiple carts
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cart> carts = new ArrayList<>();

    // One customer can have multiple orders
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    public Customer() {}

    // Convenience constructor
    public Customer(String firstName,String lastName,String email) {
    	this.firstName = firstName;
    	this.lastName = lastName;
    	this.email = email;
    }
    
    // --- getters and setters ---
    public Long getId() { 
    	return id; 
    }
    
    //For testing purposes only
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFirstName() {
    	return firstName; 
    }
    
    public void setFirstName(String firstName) { 
    	this.firstName = firstName; 
    }
    
    public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email; 
	}
	
    public void setEmail(String email) {
    	this.email = email; 
    }
    
    public List<Cart> getCarts() {
    	return carts; 
    }
    
    public List<Order> getOrders() {
    	return orders; 
    }
}


