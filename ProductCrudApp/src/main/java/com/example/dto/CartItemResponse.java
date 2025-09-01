package com.example.dto;

import com.example.entities.CartItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartItemResponse {

    private final Long id;
    private final Long productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final LocalDateTime dateAdded;

    private CartItemResponse(Long id, Long productId, String productName,
                             int quantity, BigDecimal unitPrice, LocalDateTime dateAdded) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.dateAdded = dateAdded;
    }

    public static CartItemResponse fromEntity(CartItem item) {
        return new CartItemResponse(
            item.getId(),
            item.getProduct().getId(),
            item.getProduct().getName(),
            item.getQuantity(),
            item.getProduct().getPrice(),
            item.getDateAdded()
        );
    }

    // Getters
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public LocalDateTime getDateAdded() { return dateAdded; }
}


