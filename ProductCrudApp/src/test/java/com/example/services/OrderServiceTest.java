package com.example.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.dao.CartDAO;
import com.example.dao.OrderDAO;
import com.example.dao.ProductDAO;
import com.example.entities.Cart;
import com.example.entities.CartItem;
import com.example.entities.Customer;
import com.example.entities.Order;
import com.example.entities.Product;
import com.example.entities.OrderItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class OrderServiceTest {

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private CartDAO cartDAO;

    @Mock
    private ProductDAO productDAO;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- createOrderFromCartDto ---

    @Test
    void testCreateOrderFromCart_Success() {
        Customer customer = new Customer();
        customer.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(1);

        CartItem item = new CartItem(product, 1);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setCustomer(customer);
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);
        cart.setStatus("NEW");

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 1L)).thenReturn(product);

        // Mock orderDAO save to do nothing
        doNothing().when(orderDAO).save(any(Order.class));
        doNothing().when(cartDAO).update(any(Cart.class));
        doNothing().when(productDAO).update(any(Product.class));

        // Act
        orderService.createOrderFromCartDto(1L);

        // Assert entity-level effects
        assertEquals("CHECKED_OUT", cart.getStatus());
        assertEquals(0, product.getStock().compareTo(0)); // stock deducted
        assertEquals(1, cart.getItems().get(0).getQuantity()); // cart item quantity remains
    }

    @Test
    void testCreateOrderFromCart_EmptyCart_Throws() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItems(new ArrayList<>());
        cart.setStatus("NEW");

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrderFromCartDto(1L)
        );
        assertEquals("Cannot create order from empty cart", ex.getMessage());
    }

    @Test
    void testUpdateStatus_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("NEW");

        when(orderDAO.findById(Order.class, 1L)).thenReturn(order);
        doNothing().when(orderDAO).update(any(Order.class));

        orderService.updateStatusDto(1L, "SHIPPED");

        assertEquals("SHIPPED", order.getStatus());
    }

    @Test
    void testUpdateStatus_OrderNotFound_Throws() {
        when(orderDAO.findById(Order.class, 1L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                orderService.updateStatusDto(1L, "SHIPPED")
        );
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void testGetOrder_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setTotal(new BigDecimal("200.00"));

        when(orderDAO.findById(Order.class, 1L)).thenReturn(order);

        // just check the entity fields via DTO mapping
        assertEquals(0, new BigDecimal("200.00").compareTo(orderService.getOrderDto(1L).getTotal()));
    }

    @Test
    void testGetOrder_OrderNotFound_Throws() {
        when(orderDAO.findById(Order.class, 1L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                orderService.getOrderDto(1L)
        );
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void testGetAllOrders_Success() {
        Order order1 = new Order();
        Order order2 = new Order();
        List<Order> orders = List.of(order1, order2);

        when(orderDAO.findAll(Order.class)).thenReturn(orders);

        assertEquals(2, orderService.getAllOrderDtos().size());
    }

    @Test
    void testGetOrdersByCustomer_Success() {
        Customer customer = new Customer();
        customer.setId(1L);

        Order order1 = new Order();
        order1.setCustomer(customer);
        Order order2 = new Order();
        order2.setCustomer(customer);

        List<Order> orders = List.of(order1, order2);

        when(orderDAO.findAll(Order.class)).thenReturn(orders);

        List<?> result = orderService.getOrdersByCustomerDto(1L);

        assertEquals(2, result.size());
    }
}

