package com.example.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

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

    // Default constructor required by JPA
    public Product() {}

    // Convenience constructor
    public Product(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
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
}


