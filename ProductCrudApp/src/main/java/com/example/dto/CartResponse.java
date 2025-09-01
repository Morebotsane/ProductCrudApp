
package com.example.dto;

import com.example.entities.CartStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CartResponse {

    private final Long id;
    private final CartStatus status;
    private final List<CartItemResponse> items;
    private final BigDecimal total;
    private final BigDecimal totalWithVAT;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime expiresAt;

    public CartResponse(Long id, CartStatus status, List<CartItemResponse> items,
                        BigDecimal total, BigDecimal totalWithVAT,
                        LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime expiresAt) {
        this.id = id;
        this.status = status;
        this.items = items;
        this.total = total;
        this.totalWithVAT = totalWithVAT;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
    }

    // Getters
    public Long getId() { return id; }
    public CartStatus getStatus() { return status; }
    public List<CartItemResponse> getItems() { return items; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getTotalWithVAT() { return totalWithVAT; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
