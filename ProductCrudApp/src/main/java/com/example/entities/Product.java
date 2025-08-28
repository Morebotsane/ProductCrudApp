package com.example.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name must not be empty.")
    @Size(min = 3, max = 50, message = "Product name must be between 3 and 50 characters long")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Product description cannot be empty.")
    @Size(min = 3, max = 200, message = "Product description must be between 3 and 200 characters long")
    @Column(nullable = false)
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than zero.")
    @Column(nullable = false)
    private BigDecimal price;
    
    @NotBlank(message = "Product code cannot be empty.")
    @Size(min = 3, max = 30, message = "Product code must be between 3 and 30 characters.")
    @Column(nullable = false, unique = true)
    private String productCode;
    
    @PositiveOrZero(message = "The stock value cannot be negative")
    @Column(nullable = false)
    private Integer stock;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Column(nullable = false, updatable = false)
    @PrePersist
    protected void onCreate() {
    	this.createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
    	this.updatedAt = LocalDateTime.now();
    }

    // Default constructor required by JPA
    public Product() {}

    // Convenience constructor
    public Product(String name, String description, BigDecimal price, String productCode, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.productCode = productCode;
        this.stock = stock;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpDatedAt() {
		return updatedAt;
	}

	public void setUpDatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	public LocalDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
}


