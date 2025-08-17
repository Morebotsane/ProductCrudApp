package com.example.dto;

import com.example.entities.CartItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private LocalDateTime dateAdded;

    public static CartItemResponse fromEntity(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.id = item.getId();
        response.productId = item.getProduct().getId();
        response.productName = item.getProduct().getName();
        response.quantity = item.getQuantity();
        response.unitPrice = item.getProduct().getPrice();
        response.dateAdded = item.getDateAdded();
        return response;
    }
    
    //getters and setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public LocalDateTime getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(LocalDateTime dateAdded) {
		this.dateAdded = dateAdded;
	}
}

