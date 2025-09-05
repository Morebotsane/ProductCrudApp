/*package com.example.services;

import com.example.dao.OrderDAO;
import com.example.dao.OrderStatusHistoryDAO;
import com.example.dao.ShipmentDAO;
import com.example.dto.OrderResponse;
//import com.example.dto.mappers.OrderMapper;
//import com.example.dto.mappers.ShippingMapper;
import com.example.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShippingServiceTest {

    @InjectMocks
    private ShippingService shippingService;

    @Mock private OrderDAO orderDAO;
    @Mock private ShipmentDAO shipmentDAO;
    @Mock private OrderStatusHistoryDAO orderStatusHistoryDAO;

    private Order paidOrder;
    private Order shippedOrder;
    private Shipment shipment;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        paidOrder = new Order();
        paidOrder.setId(1L);
        paidOrder.setStatus(OrderStatus.PAID);

        shippedOrder = new Order();
        shippedOrder.setId(2L);
        shippedOrder.setStatus(OrderStatus.SHIPPED);

        shipment = new Shipment();
        shipment.setId(1L);
        shipment.setOrder(shippedOrder);
        shipment.setCarrier("DHL");
        shipment.setShippedAt(LocalDateTime.now());
    }

    @Test
    void testShipOrder_Success() {
        when(orderDAO.findById(Order.class, 1L)).thenReturn(paidOrder);

        OrderResponse response = shippingService.shipOrder(1L, "DHL");

        assertNotNull(response);
        assertEquals(OrderStatus.SHIPPED, paidOrder.getStatus());
        verify(shipmentDAO, times(1)).save(any(Shipment.class));
        verify(orderDAO, times(1)).update(paidOrder);
        verify(orderStatusHistoryDAO, times(1)).save(any(OrderStatusHistory.class));
    }

    @Test
    void testShipOrder_InvalidStatus() {
        Order newOrder = new Order();
        newOrder.setId(99L);
        newOrder.setStatus(OrderStatus.NEW);

        when(orderDAO.findById(Order.class, 99L)).thenReturn(newOrder);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> shippingService.shipOrder(99L, "FedEx"));

        assertEquals("Only PAID orders can be shipped", ex.getMessage());
    }

    @Test
    void testDeliverOrder_Success() {
        when(orderDAO.findById(Order.class, 2L)).thenReturn(shippedOrder);
        when(shipmentDAO.findByOrder(shippedOrder)).thenReturn(shipment);

        OrderResponse response = shippingService.deliverOrder(2L);

        assertNotNull(response);
        assertEquals(OrderStatus.DELIVERED, shippedOrder.getStatus());
        assertNotNull(shipment.getDeliveredAt());
        verify(shipmentDAO, times(1)).update(shipment);
        verify(orderDAO, times(1)).update(shippedOrder);
        verify(orderStatusHistoryDAO, times(1)).save(any(OrderStatusHistory.class));
    }

    @Test
    void testDeliverOrder_InvalidStatus() {
        when(orderDAO.findById(Order.class, 1L)).thenReturn(paidOrder);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> shippingService.deliverOrder(1L));

        assertEquals("Only SHIPPED orders can be delivered", ex.getMessage());
    }
}

*/