package com.example.dto.mappers;

import com.example.dto.ShippingDTO;
import com.example.entities.Shipment;

public class ShippingMapper {

    public static ShippingDTO toDto(Shipment shipment) {
        if (shipment == null) return null;

        ShippingDTO dto = new ShippingDTO();
        dto.setOrderId(shipment.getOrder() != null ? shipment.getOrder().getId() : null);
        dto.setCarrier(shipment.getCarrier());
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setShippedAt(shipment.getShippedAt());
        dto.setDeliveredAt(shipment.getDeliveredAt());

        return dto;
    }
}
