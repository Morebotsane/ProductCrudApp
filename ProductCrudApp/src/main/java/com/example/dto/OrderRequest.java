package com.example.dto;

public class OrderRequest {
    private Long cartId;
    private String status;  // For updates, optional in creation

    public OrderRequest() {}

    public OrderRequest(String status) {
        this.status = status;
    }
    
    public Long getCartId() {
        return cartId;
    }
    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}

