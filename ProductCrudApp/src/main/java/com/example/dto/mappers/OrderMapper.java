package com.example.dto.mappers;

import com.example.dto.AddressSnapshotResponse;
import com.example.dto.OrderItemResponse;
import com.example.dto.OrderResponse;
import com.example.entities.AddressSnapshot;
import com.example.entities.Order;
import com.example.entities.OrderItem;
import com.example.entities.OrderStatus;

//import java.util.List;
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

        dto.setItems(order.getItems() != null
                ? order.getItems().stream().map(OrderMapper::toItemDto).collect(Collectors.toList())
                : null);

        dto.setShippingAddress(toAddressSnapshotDto(order.getShippingAddress()));
        return dto;
    }

    private static OrderItemResponse toItemDto(OrderItem item) {
        if (item == null) return null;

        OrderItemResponse dto = new OrderItemResponse();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getUnitPrice());
        return dto;
    }

    public static AddressSnapshotResponse toAddressSnapshotDto(AddressSnapshot snapshot) {
        if (snapshot == null) return null;

        AddressSnapshotResponse dto = new AddressSnapshotResponse();
        dto.setLine1(snapshot.getLine1());
        dto.setLine2(snapshot.getLine2());
        dto.setCity(snapshot.getCity());
        dto.setRegion(snapshot.getRegion());
        dto.setPostalCode(snapshot.getPostalCode());
        dto.setCountry(snapshot.getCountry());
        return dto;
    }

    public static OrderStatus fromStatusString(String status) {
        try {
            return status != null ? OrderStatus.valueOf(status) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
