/*package com.example.services;

import com.example.dao.*;
import com.example.dto.OrderResponse;
import com.example.entities.*;
import com.example.entities.AddressType;
import com.example.entities.CartStatus;
import com.example.entities.OrderStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private CartDAO cartDAO;
    @Mock
    private ProductDAO productDAO;
    @Mock
    private OrderDAO orderDAO;
    @Mock
    private AddressDAO addressDAO;
    @Mock
    private PaymentDAO paymentDAO;
    @Mock
    private OrderStatusHistoryDAO orderStatusHistoryDAO;

    private Customer customer;
    private Cart cart;
    private Address defaultAddress;
    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customer = new Customer("John", "Doe", "john@example.com", "1234567890");
        customer.setId(1L);

        defaultAddress = new Address();
        defaultAddress.setId(100L);
        defaultAddress.setLine1("123 Main St");
        defaultAddress.setCity("Metropolis");
        defaultAddress.setPostalCode("12345");
        defaultAddress.setCountry("USA");
        defaultAddress.setType(AddressType.SHIPPING);
        defaultAddress.setDefault(true);
        defaultAddress.setCustomer(customer);

        product = new Product();
        product.setId(10L);
        product.setName("Laptop");
        product.setPrice(new BigDecimal("1000"));
        product.setStock(5);

        cart = new Cart();
        cart.setId(1L);
        cart.setCustomer(customer);
        cart.setStatus(CartStatus.NEW);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setCart(cart);
        cart.getItems().add(item);
    }

    @Test
    void testCreateOrderFromCart_Success() {
        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 10L)).thenReturn(product);
        when(addressDAO.findDefaultShippingByCustomer(customer)).thenReturn(Optional.of(defaultAddress));
        doNothing().when(orderDAO).save(any(Order.class));
        doNothing().when(cartDAO).update(cart);
        doNothing().when(productDAO).update(product);

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
    void testCreateOrder_EmptyCart_Throws() {
        cart.getItems().clear();
        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrderFromCartDto(1L));
        assertEquals("Cannot create order from empty cart", ex.getMessage());
    }

    @Test
    void testCreateOrder_AlreadyCheckedOut_Throws() {
        cart.setStatus(CartStatus.CHECKED_OUT);
        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        Exception ex = assertThrows(IllegalStateException.class,
                () -> orderService.createOrderFromCartDto(1L));
        assertEquals("Cart already checked out", ex.getMessage());
    }

    @Test
    void testCreateOrder_NoDefaultShippingAddress_Throws() {
        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(addressDAO.findDefaultShippingByCustomer(customer)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrderFromCartDto(1L));
        assertEquals("Customer has no default shipping address", ex.getMessage());
    }

    @Test
    void testCreateOrder_InsufficientStock_Throws() {
        product.setStock(1); // less than cart quantity
        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 10L)).thenReturn(product);
        when(addressDAO.findDefaultShippingByCustomer(customer)).thenReturn(Optional.of(defaultAddress));

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrderFromCartDto(1L));
        assertEquals("Insufficient stock for product: Laptop", ex.getMessage());
    }

    @Test
    void testUpdateStatus_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID); // initial status

        when(orderDAO.findById(Order.class, 1L)).thenReturn(order);

        OrderResponse response = orderService.updateStatusDto(1L, OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        verify(orderDAO).update(order);
        assertNotNull(response);
    }

    @Test
    void testUpdateStatus_OrderNotFound_Throws() {
        when(orderDAO.findById(Order.class, 99L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.updateStatusDto(99L, OrderStatus.SHIPPED));
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void testGetOrderDto_Success() {
        Order order = new Order();
        order.setId(1L);
        when(orderDAO.findById(Order.class, 1L)).thenReturn(order);

        OrderResponse response = orderService.getOrderDto(1L);
        assertNotNull(response);
    }

    @Test
    void testGetOrderDto_NotFound_Throws() {
        when(orderDAO.findById(Order.class, 99L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.getOrderDto(99L));
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void testGetOrdersByCustomerDto() {
        Customer otherCustomer = new Customer("Jane", "Doe", "jane@example.com", "9999999999");
        otherCustomer.setId(2L);

        Order order1 = new Order();
        order1.setId(1L);
        order1.setCustomer(customer);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setCustomer(customer);

        Order order3 = new Order();
        order3.setId(3L);
        order3.setCustomer(otherCustomer);

        when(orderDAO.findAll(Order.class)).thenReturn(Arrays.asList(order1, order2, order3));

        var responses = orderService.getOrdersByCustomerDto(customer.getId());

        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.getCustomerId().equals(customer.getId())));
    }

    @Test
    void testGetAllOrderDtos() {
        Order order1 = new Order();
        order1.setId(1L);
        Order order2 = new Order();
        order2.setId(2L);

        when(orderDAO.findAll(Order.class)).thenReturn(Arrays.asList(order1, order2));

        var responses = orderService.getAllOrderDtos();

        assertEquals(2, responses.size());
    }
}
*/