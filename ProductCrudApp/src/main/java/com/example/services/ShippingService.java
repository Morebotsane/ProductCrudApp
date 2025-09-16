package com.example.services;

import com.example.dao.OrderDAO;
import com.example.dao.ShipmentDAO;
import com.example.dao.OrderStatusHistoryDAO;
import com.example.dto.OrderResponse;
import com.example.dto.mappers.OrderMapper;
import com.example.dto.mappers.ShippingMapper;
import com.example.entities.*;
import com.example.security.JwtTokenService;
import com.example.security.exceptions.UnauthorizedAccessException;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@Stateless
public class ShippingService {

    @Inject private OrderDAO orderDAO;
    @Inject private ShipmentDAO shipmentDAO;
    @Inject private OrderStatusHistoryDAO orderStatusHistoryDAO;
    @Inject private AuditService auditService;
    @Inject private JwtTokenService jwtTokenService;

    // -------------------------
    // SHIP ORDER (Admin only)
    // -------------------------
    @Transactional
    public OrderResponse shipOrder(Long orderId, String carrier) {
        enforceAdminRole();

        Order order = fetchOrder(orderId);
        validateStatus(order, OrderStatus.PAID, "Only PAID orders can be shipped");

        updateOrderStatus(order, OrderStatus.SHIPPED);

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setCarrier(carrier);
        shipment.setShippedAt(LocalDateTime.now());
        shipment.setTrackingNumber("TRK-" + System.currentTimeMillis());
        shipmentDAO.save(shipment);
        shipmentDAO.getEntityManager().flush();

        logStatusChange(order, OrderStatus.PAID, OrderStatus.SHIPPED);

        auditService.record(
                jwtTokenService.getUsername(),
                "SHIP_ORDER",
                "Order",
                orderId,
                String.format("{\"carrier\": \"%s\", \"trackingNumber\": \"%s\"}",
                        carrier, shipment.getTrackingNumber())
        );

        return mapOrderToResponse(order);
    }

    // -------------------------
    // DELIVER ORDER (Admin only)
    // -------------------------
    @Transactional
    public OrderResponse deliverOrder(Long orderId) {
        enforceAdminRole();

        Order order = fetchOrder(orderId);
        validateStatus(order, OrderStatus.SHIPPED, "Only SHIPPED orders can be delivered");

        Shipment shipment = shipmentDAO.findByOrder(order);
        if (shipment == null) throw new IllegalStateException("Shipment not found");

        updateOrderStatus(order, OrderStatus.DELIVERED);

        shipment.setDeliveredAt(LocalDateTime.now());
        shipmentDAO.update(shipment);
        shipmentDAO.getEntityManager().flush();

        logStatusChange(order, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

        auditService.record(
                jwtTokenService.getUsername(),
                "DELIVER_ORDER",
                "Order",
                orderId,
                String.format("{\"shipmentId\": %d, \"deliveredAt\": \"%s\"}",
                        shipment.getId(), shipment.getDeliveredAt())
        );

        return mapOrderToResponse(order);
    }

    // -------------------------
    // PRIVATE HELPERS
    // -------------------------
    private Order fetchOrder(Long orderId) {
        Order order = orderDAO.findById(Order.class, orderId);
        if (order == null) throw new IllegalArgumentException("Order not found");
        return order;
    }

    private void validateStatus(Order order, OrderStatus expected, String message) {
        if (order.getStatus() != expected) throw new IllegalStateException(message);
    }

    private void updateOrderStatus(Order order, OrderStatus newStatus) {
        order.setStatus(newStatus);
        orderDAO.update(order);
        orderDAO.getEntityManager().flush();
    }

    private void logStatusChange(Order order, OrderStatus from, OrderStatus to) {
        orderStatusHistoryDAO.save(new OrderStatusHistory(order, from, to));
        orderStatusHistoryDAO.getEntityManager().flush();
    }

    private OrderResponse mapOrderToResponse(Order order) {
        OrderResponse dto = OrderMapper.toDto(order);
        dto.setShipment(ShippingMapper.toDto(shipmentDAO.findByOrder(order)));
        return dto;
    }

    // -------------------------
    // ROLE ENFORCEMENT
    // -------------------------
    private void enforceAdminRole() {
        if (!jwtTokenService.isAdmin()) {
            throw new UnauthorizedAccessException("Only admins can perform shipping operations");
        }
    }
}