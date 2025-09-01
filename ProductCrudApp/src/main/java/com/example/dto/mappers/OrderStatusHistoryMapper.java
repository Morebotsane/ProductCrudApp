package com.example.dto.mappers;

import com.example.dto.OrderStatusHistoryDTO;
import com.example.entities.OrderStatusHistory;

import java.util.List;
import java.util.stream.Collectors;

public class OrderStatusHistoryMapper {

    public static OrderStatusHistoryDTO toDto(OrderStatusHistory history) {
        if (history == null) return null;

        OrderStatusHistoryDTO dto = new OrderStatusHistoryDTO();
        dto.setId(history.getId());
        dto.setOrderId(history.getOrder() != null ? history.getOrder().getId() : null);
        dto.setFromStatus(history.getFromStatus());
        dto.setToStatus(history.getToStatus());
        dto.setChangedAt(history.getChangedAt());
        return dto;
    }

    public static List<OrderStatusHistoryDTO> toDto(List<OrderStatusHistory> histories) {
        return histories != null
                ? histories.stream().map(OrderStatusHistoryMapper::toDto).collect(Collectors.toList())
                : null;
    }
}
