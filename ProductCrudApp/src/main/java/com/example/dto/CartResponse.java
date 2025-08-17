package com.example.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {
    private Long id;
    private String status;
    private List<CartItemResponse> items; // Use DTO, not entity
    private BigDecimal total;
    private BigDecimal totalWithVAT;

    public CartResponse() {}

    public CartResponse(Long id, String status, List<CartItemResponse> items,
                        BigDecimal total, BigDecimal totalWithVAT) {
        this.id = id;
        this.status = status;
        this.items = items;
        this.total = total;
        this.totalWithVAT = totalWithVAT;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<CartItemResponse> getItems() { return items; }
    public void setItems(List<CartItemResponse> items) { this.items = items; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getTotalWithVAT() { return totalWithVAT; }
    public void setTotalWithVAT(BigDecimal totalWithVAT) { this.totalWithVAT = totalWithVAT; }
}
