package com.example.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotBlank
    private String line1;

    private String line2;

    @NotBlank
    private String city;

    private String region;

    @NotBlank
    private String postalCode;

    @NotBlank
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType type; // BILLING or SHIPPING

    private boolean isDefault;

    public Address() {}

    public Address(Customer customer, String line1, String city, String postalCode, String country, AddressType type) {
        this.customer = customer;
        this.line1 = line1;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.type = type;
    }

    // --- Getters and setters ---
    public Long getId() {
    	return id; 
    }
    //this is done just so that the unit test will go through without a hitch!
    public void setId(Long id) {
    	this.id = id;
    }
    
    
    public Customer getCustomer() {
    	return customer; 
    }
    
    public void setCustomer(Customer customer) {
    	this.customer = customer;
    }
    public String getLine1() {
    	return line1; 
    }
    
    public void setLine1(String line1) { 
    	this.line1 = line1; 
    }
    
    public String getLine2() {
    	return line2; 
    }
    
    public void setLine2(String line2) {
    	this.line2 = line2;
    }
    
    public String getCity() {
    	return city;
    }
    
    public void setCity(String city) {
    	this.city = city; 
    }
    
    public String getRegion() {
    	return region; 
    }
    
    public void setRegion(String region) {
    	this.region = region;
    }
    
    public String getPostalCode() {
    	return postalCode; 
    }
    
    public void setPostalCode(String postalCode) {
    	this.postalCode = postalCode; 
    }
    
    public String getCountry() {
    	return country; 
    }
    
    public void setCountry(String country) {
    	this.country = country; 
    }
    
    public AddressType getType() {
    	return type; 
    }
    
    public void setType(AddressType type) {
    	this.type = type; 
    }
    
    public boolean isDefault() { 
    	return isDefault; 
    }
    
    public void setDefault(boolean aDefault) {
    	isDefault = aDefault;
    }
}