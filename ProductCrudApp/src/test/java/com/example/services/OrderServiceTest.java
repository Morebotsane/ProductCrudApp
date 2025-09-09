package com.example.services;

import com.example.dao.*;
import com.example.dto.OrderResponse;
import com.example.entities.*;
import com.example.entities.AddressType;
import com.example.entities.CartStatus;
import com.example.entities.OrderStatus;
import com.example.entities.PaymentMethod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock private CartDAO cartDAO;
    @Mock private ProductDAO productDAO;
    @Mock private OrderDAO orderDAO;
    @Mock private AddressDAO addressDAO;
    @Mock private PaymentDAO paymentDAO;
    @Mock private OrderStatusHistoryDAO orderStatusHistoryDAO;

    @Mock private JsonWebToken jwt;
    @Mock private SecurityContext securityContext;

    private Customer customer;
    private Cart cart;
    private Address defaultAddress;
    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Customer
        customer = new Customer("John", "Doe", "john@example.com", "1234567890");
        customer.setId(1L);

        // Default address
        defaultAddress = new Address();
        defaultAddress.setId(100L);
        defaultAddress.setLine1("123 Main St");
        defaultAddress.setCity("Metropolis");
        defaultAddress.setPostalCode("12345");
        defaultAddress.setCountry("USA");
        defaultAddress.setType(AddressType.SHIPPING);
        defaultAddress.setDefault(true);
        defaultAddress.setCustomer(customer);

        // Product
        product = new Product();
        product.setId(10L);
        product.setName("Laptop");
        product.setPrice(new BigDecimal("1000"));
        product.setStock(5);

        // Cart
        cart = new Cart();
        cart.setId(1L);
        cart.setCustomer(customer);
        cart.setStatus(CartStatus.NEW);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setCart(cart);
        cart.getItems().add(item);

        // Mock JWT
        when(jwt.getSubject()).thenReturn("1"); // customerId = 1
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
    }

    @Test
    void testCreateOrderFromCart_Success() {
        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 10L)).thenReturn(product);
        when(addressDAO.findDefaultShippingByCustomer(customer)).thenReturn(Optional.of(defaultAddress));
        doNothing().when(orderDAO).save(any(Order.class));
        doNothing().when(cartDAO).update(cart);
        doNothing().when(productDAO).update(product);
        doNothing().when(orderStatusHistoryDAO).save(any(OrderStatusHistory.class));

        OrderResponse response = orderService.createOrderFromCartDto(1L);

        assertNotNull(response);
        assertEquals(CartStatus.CHECKED_OUT, cart.getStatus());
        assertEquals(3, product.getStock());
        assertNotNull(response.getShippingAddress());
        assertEquals("123 Main St", response.getShippingAddress().getLine1());

        verify(orderDAO).save(any(Order.class));
        verify(cartDAO).update(cart);
        verify(productDAO).update(product);
    }

    @Test
    void testCreateOrder_OtherCustomer_ThrowsSecurityException() {
        Customer other = new Customer("Jane", "Doe", "jane@example.com", "9999999999");
        other.setId(2L);
        cart.setCustomer(other);
        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(jwt.getSubject()).thenReturn("1");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> orderService.createOrderFromCartDto(1L));
        assertEquals("Forbidden: cannot create order from another customer's cart", ex.getMessage());
    }

    @Test
    void testPayOrder_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setTotal(new BigDecimal("2000"));
        order.setStatus(OrderStatus.NEW);
        order.setCustomer(customer);

        when(orderDAO.findById(Order.class, 1L)).thenReturn(order);
        doNothing().when(paymentDAO).save(any(Payment.class));
        doNothing().when(orderDAO).update(order);
        doNothing().when(orderStatusHistoryDAO).save(any(OrderStatusHistory.class));
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);

        OrderResponse response = orderService.payOrder(1L, new BigDecimal("2000"), PaymentMethod.CARD, "TXN123");

        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(paymentDAO).save(any(Payment.class));
        verify(orderDAO).update(order);
    }

    @Test
    void testPayOrder_FailedDueToInsufficientAmount() {
        Order order = new Order();
        order.setId(1L);
        order.setTotal(new BigDecimal("2000"));
        order.setStatus(OrderStatus.NEW);
        order.setCustomer(customer);

        when(orderDAO.findById(Order.class, 1L)).thenReturn(order);
        doNothing().when(paymentDAO).save(any(Payment.class));
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);

        OrderResponse response = orderService.payOrder(1L, new BigDecimal("1000"), PaymentMethod.CARD, "TXN124");

        assertEquals(OrderStatus.NEW, order.getStatus());
        verify(paymentDAO).save(any(Payment.class));
    }

    @Test
    void testUpdateStatus_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);

        when(orderDAO.findById(Order.class, 1L)).thenReturn(order);
        doNothing().when(orderStatusHistoryDAO).save(any(OrderStatusHistory.class));

        OrderResponse response = orderService.updateStatusDto(1L, OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        verify(orderDAO).update(order);
        assertNotNull(response);
    }

    @Test
    void testGetOrdersByCustomerDto_OwnershipEnforced() {
        Order order1 = new Order(); order1.setId(1L); order1.setCustomer(customer);
        Order order2 = new Order(); order2.setId(2L); order2.setCustomer(customer);
        Customer other = new Customer("Jane", "Doe", "jane@example.com", "9999999999");
        other.setId(2L);
        Order order3 = new Order(); order3.setId(3L); order3.setCustomer(other);

        when(orderDAO.findAll(Order.class)).thenReturn(Arrays.asList(order1, order2, order3));

        var responses = orderService.getOrdersByCustomerDto(customer.getId());

        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.getCustomerId().equals(customer.getId())));
    }
}

