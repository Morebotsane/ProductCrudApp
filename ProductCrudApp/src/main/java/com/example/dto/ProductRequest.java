package com.example.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductRequest {

    @NotBlank(message = "Product name must not be empty")
    @Size(min = 3, max = 50, message = "Product name must be between 3 and 50 characters")
    private String name;

    @NotBlank(message = "Product description cannot be empty")
    @Size(min = 3, max = 255, message = "Description must be between 3 and 255 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @NotBlank(message = "Product code must not be empty")
    @Size(min = 3, max = 20, message = "Product code must be between 3 and 20 characters")
    private String productCode;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    public ProductRequest() {}

    public ProductRequest(String name, String description, BigDecimal price, String productCode, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.productCode = productCode;
        this.stock = stock;
    }

    // Getters and setters
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

    public String getProductCode() {
        return productCode;
    }
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Integer getStock() {
        return stock;
    }
    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
