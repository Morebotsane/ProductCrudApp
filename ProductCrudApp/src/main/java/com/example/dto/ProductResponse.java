package com.example.dto;

import com.example.entities.Product;
import java.math.BigDecimal;

public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;

    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.id = product.getId();
        response.name = product.getName();
        response.description = product.getDescription();
        response.price = product.getPrice();
        return response;
    }

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

