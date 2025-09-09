package com.example.services;

import com.example.dao.OrderDAO;
import com.example.dao.OrderStatusHistoryDAO;
import com.example.dao.ShipmentDAO;
import com.example.dto.OrderResponse;
import com.example.entities.*;
import jakarta.ws.rs.ForbiddenException;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.SecurityContext;

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
    @Mock private AuditService auditService;

    @Mock private EntityManager orderEm;
    @Mock private EntityManager shipmentEm;
    @Mock private EntityManager historyEm;

    @Mock private SecurityContext securityContext;
    @Mock private JsonWebToken jwt;

    private Order paidOrder;
    private Order shippedOrder;
    private Shipment shipment;
    private Customer customer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock EntityManager flushes to avoid NPE
        when(orderDAO.getEntityManager()).thenReturn(orderEm);
        when(shipmentDAO.getEntityManager()).thenReturn(shipmentEm);
        when(orderStatusHistoryDAO.getEntityManager()).thenReturn(historyEm);
        doNothing().when(orderEm).flush();
        doNothing().when(shipmentEm).flush();
        doNothing().when(historyEm).flush();

        // Customer
        customer = new Customer("John", "Doe", "john@example.com", "1234567890");
        customer.setId(1L);

        // Orders
        paidOrder = new Order();
        paidOrder.setId(1L);
        paidOrder.setStatus(OrderStatus.PAID);
        paidOrder.setCustomer(customer);

        shippedOrder = new Order();
        shippedOrder.setId(2L);
        shippedOrder.setStatus(OrderStatus.SHIPPED);
        shippedOrder.setCustomer(customer);

        // Shipment
        shipment = new Shipment();
        shipment.setId(1L);
        shipment.setOrder(shippedOrder);
        shipment.setCarrier("DHL");
        shipment.setShippedAt(LocalDateTime.now());

        // JWT/ SecurityContext
        when(jwt.getSubject()).thenReturn("1");
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
    }

    @Test
    void testShipOrder_Success() {
        when(orderDAO.findById(Order.class, 1L)).thenReturn(paidOrder);
        doNothing().when(shipmentDAO).save(any(Shipment.class));

        OrderResponse response = shippingService.shipOrder(1L, "DHL");

        assertNotNull(response);
        assertEquals(OrderStatus.SHIPPED, paidOrder.getStatus());
        verify(shipmentDAO).save(any(Shipment.class));
        verify(orderDAO).update(paidOrder);
        verify(orderStatusHistoryDAO).save(any(OrderStatusHistory.class));
        verify(auditService).record(eq("system"), eq("SHIP_ORDER"), eq("Order"), eq(1L), contains("DHL"));
    }

    @Test
    void testShipOrder_OtherCustomer_ThrowsSecurityException() {
        // Set a different customer to simulate ownership violation
        Customer other = new Customer("Jane", "Doe", "jane@example.com", "9999999999");
        other.setId(2L);
        paidOrder.setCustomer(other);

        when(orderDAO.findById(Order.class, 1L)).thenReturn(paidOrder);

        // Expect SecurityException instead of ForbiddenException
        SecurityException ex = assertThrows(SecurityException.class,
                () -> shippingService.shipOrder(1L, "FedEx"));

        assertEquals("Cannot access another customer's order", ex.getMessage());
    }

    @Test
    void testShipOrder_InvalidStatus() {
        Order newOrder = new Order();
        newOrder.setId(99L);
        newOrder.setStatus(OrderStatus.NEW);
        newOrder.setCustomer(customer);
        when(orderDAO.findById(Order.class, 99L)).thenReturn(newOrder);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> shippingService.shipOrder(99L, "FedEx"));

        assertEquals("Only PAID orders can be shipped", ex.getMessage());
    }

    @Test
    void testDeliverOrder_Success() {
        when(orderDAO.findById(Order.class, 2L)).thenReturn(shippedOrder);
        when(shipmentDAO.findByOrder(shippedOrder)).thenReturn(shipment);
        doNothing().when(shipmentDAO).update(any(Shipment.class));

        OrderResponse response = shippingService.deliverOrder(2L);

        assertNotNull(response);
        assertEquals(OrderStatus.DELIVERED, shippedOrder.getStatus());
        assertNotNull(shipment.getDeliveredAt());

        verify(shipmentDAO).update(shipment);
        verify(orderDAO).update(shippedOrder);
        verify(orderStatusHistoryDAO).save(any(OrderStatusHistory.class));
        verify(auditService).record(eq("system"), eq("DELIVER_ORDER"), eq("Order"), eq(2L), contains("deliveredAt"));
    }

    @Test
    void testDeliverOrder_InvalidStatus() {
        when(orderDAO.findById(Order.class, 1L)).thenReturn(paidOrder);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> shippingService.deliverOrder(1L));

        assertEquals("Only SHIPPED orders can be delivered", ex.getMessage());
    }

    @Test
    void testDeliverOrder_ShipmentNotFound() {
        when(orderDAO.findById(Order.class, 2L)).thenReturn(shippedOrder);
        when(shipmentDAO.findByOrder(shippedOrder)).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> shippingService.deliverOrder(2L));

        assertEquals("Shipment not found", ex.getMessage());
    }
}

