package com.example.dto;

import com.example.entities.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponse {
    private Long id;
    private String status;
    private LocalDateTime orderDate;
    private BigDecimal total;
    private CustomerSummary customer;
    private List<OrderItemResponse> items;

    public static OrderResponse fromEntity(Order order) {
        OrderResponse response = new OrderResponse();
        response.id = order.getId();
        response.status = order.getStatus();
        response.orderDate = order.getOrderDate();
        response.total = order.getTotal();

        if (order.getCustomer() != null) {
            response.customer = new CustomerSummary(
                order.getCustomer().getId(),
                order.getCustomer().getFirstName(),//U Khutlele mona
                order.getCustomer().getEmail()
            );
        }

        if (order.getItems() != null) {
            response.items = order.getItems().stream()
                    			  .map(OrderItemResponse::fromEntity)
                    			  .collect(Collectors.toList());
        }
        return response;
    }

    public static class CustomerSummary {
        private Long id;
        private String name;
        private String email;

        public CustomerSummary(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        public Long getId() {
        	return id; 
        }
        
        public String getName() { 
        	return name; 
        }
        
        public String getEmail() {
        	return email;
        }
    }

    // Getters
    public Long getId() {
    	return id; 
    }
    
    public String getStatus() { 
    	return status; 
    }
    
    public LocalDateTime getOrderDate() {
    	return orderDate;
    }
    
    public BigDecimal getTotal() {
    	return total;
    }
    
    public CustomerSummary getCustomer() { 
    	return customer; 
    }
    
    public List<OrderItemResponse> getItems() { 
    	return items;
    }
}
