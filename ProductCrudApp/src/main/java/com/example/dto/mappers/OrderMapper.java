package com.example.dto.mappers;

import com.example.dto.OrderItemResponse;
import com.example.dto.OrderResponse;
import com.example.entities.Order;
import com.example.entities.OrderItem;

import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderResponse toDto(Order order) {
        if (order == null) return null;

        OrderResponse dto = new OrderResponse();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotal(order.getTotal());
        dto.setCustomerId(order.getCustomer() != null ? order.getCustomer().getId() : null);

        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(OrderMapper::toItemDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private static OrderItemResponse toItemDto(OrderItem item) {
        OrderItemResponse dto = new OrderItemResponse();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getUnitPrice());
        return dto;
    }
}




