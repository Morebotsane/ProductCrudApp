package com.example.dto;

import com.example.entities.OrderItem;
import java.math.BigDecimal;

public class OrderItemResponse {
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;

    public static OrderItemResponse fromEntity(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.productId = orderItem.getProduct().getId();
        response.productName = orderItem.getProduct().getName();
        response.unitPrice = orderItem.getUnitPrice();
        response.quantity = orderItem.getQuantity();
        // Calculate line total if not set in entity
        if (orderItem.getLineTotal() != null) {
            response.lineTotal = orderItem.getLineTotal();
        } else {
            response.lineTotal = response.unitPrice.multiply(BigDecimal.valueOf(response.quantity));
        }
        return response;
    }

    // Getters
    public Long getProductId() { 
    	return productId; 
    }
    
    public String getProductName() { 
    	return productName;
    }
    
    public BigDecimal getUnitPrice() {
    	return unitPrice; 
    }
    
    public int getQuantity() { 
    	return quantity; 
    }
    
    public BigDecimal getLineTotal() {
    	return lineTotal; 
    }
}
