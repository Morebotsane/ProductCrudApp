package com.example.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.dao.CartDAO;
import com.example.dao.OrderDAO;
import com.example.dto.OrderResponse;
import com.example.entities.Cart;
import com.example.entities.CartItem;
import com.example.entities.Customer;
import com.example.entities.Order;
import com.example.entities.Product;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OrderServiceTest {

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private CartDAO cartDAO;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Cart buildSampleCart() {
        Customer customer = new Customer();
        customer.setId(100L);

        Product product = new Product();
        product.setId(200L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("10.00"));

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setCustomer(customer);
        cart.setStatus("OPEN");
        cart.setItems(Collections.singletonList(item));

        return cart;
    }

    @Test
    void testCreateOrderFromCartDto() {
        Cart cart = buildSampleCart();
        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        OrderResponse response = orderService.createOrderFromCartDto(1L);

        assertNotNull(response);
        assertEquals("NEW", response.getStatus());
        assertEquals(1, response.getItems().size());
        verify(orderDAO, times(1)).save(any(Order.class));
        verify(cartDAO, times(1)).update(cart);
    }

    @Test
    void testUpdateStatusDto() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("PENDING");

        when(orderDAO.findById(Order.class, 1L)).thenReturn(order);

        OrderResponse response = orderService.updateStatusDto(1L, "SHIPPED");

        assertNotNull(response);
        assertEquals("SHIPPED", response.getStatus());
        verify(orderDAO, times(1)).update(order);
    }

    @Test
    void testGetOrderDto() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("NEW");

        when(orderDAO.findById(Order.class, 1L)).thenReturn(order);

        OrderResponse response = orderService.getOrderDto(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testGetAllOrderDtos() {
        Order order1 = new Order();
        order1.setId(1L);
        Order order2 = new Order();
        order2.setId(2L);

        when(orderDAO.findAll(Order.class)).thenReturn(Arrays.asList(order1, order2));

        List<OrderResponse> responses = orderService.getAllOrderDtos();

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(2L, responses.get(1).getId());
    }

    @Test
    void testGetOrdersByCustomerDto() {
        Customer customer = new Customer();
        customer.setId(100L);

        Order order1 = new Order();
        order1.setId(1L);
        order1.setCustomer(customer);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setCustomer(customer);

        when(orderDAO.findAll(Order.class)).thenReturn(Arrays.asList(order1, order2));

        List<OrderResponse> responses = orderService.getOrdersByCustomerDto(100L);

        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(o -> o.getId() == 1L || o.getId() == 2L));
    }
}
