package com.example.services;

import com.example.dao.CartDAO;
import com.example.dao.CustomerDAO;
import com.example.dao.ProductDAO;
import com.example.entities.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartDAO cartDAO;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private CustomerDAO customerDAO;

    private AutoCloseable closeable;

    private Customer customer;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        customer = new Customer();
        customer.setId(1L);

        product = new Product();
        product.setId(100L);
        product.setName("Laptop");
        product.setStock(10);
        product.setPrice(new BigDecimal("1000"));

        cart = new Cart();
        cart.setId(200L);
        cart.setCustomer(customer);
        cart.setStatus(CartStatus.NEW);
        cart.setItems(new ArrayList<>());
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        cart.setExpiresAt(LocalDateTime.now().plusHours(2));
    }

    // -------------------------
    // getOrCreateActiveCart
    // -------------------------

    @Test
    void testGetOrCreateActiveCart_ReturnsExisting() {
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);
        when(cartDAO.findActiveCartByCustomerId(1L)).thenReturn(cart);

        Cart result = cartService.getOrCreateActiveCart(1L);

        assertEquals(cart, result);
        verify(cartDAO, never()).save(any());
    }

    @Test
    void testGetOrCreateActiveCart_CreatesNew() {
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);
        when(cartDAO.findActiveCartByCustomerId(1L)).thenReturn(null);

        Cart result = cartService.getOrCreateActiveCart(1L);

        assertEquals(CartStatus.NEW, result.getStatus());
        verify(cartDAO).save(any(Cart.class));
    }

    @Test
    void testGetOrCreateActiveCart_CustomerNotFound() {
        when(customerDAO.findById(Customer.class, 99L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> cartService.getOrCreateActiveCart(99L));
    }

    // -------------------------
    // addProduct
    // -------------------------

    @Test
    void testAddProduct_AddsNewItem() {
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 100L)).thenReturn(product);

        Cart result = cartService.addProduct(200L, 100L, 2);

        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getItems().get(0).getQuantity());
        verify(cartDAO).update(cart);
    }

    @Test
    void testAddProduct_IncrementsExistingItem() {
        CartItem item = new CartItem(product, 2);
        item.setCart(cart);
        cart.getItems().add(item);

        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 100L)).thenReturn(product);

        Cart result = cartService.addProduct(200L, 100L, 3);

        assertEquals(1, result.getItems().size());
        assertEquals(5, result.getItems().get(0).getQuantity());
    }

    @Test
    void testAddProduct_ProductNotFound() {
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> cartService.addProduct(200L, 999L, 1));
    }

    @Test
    void testAddProduct_InsufficientStock() {
        product.setStock(1);
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 100L)).thenReturn(product);

        assertThrows(IllegalArgumentException.class, () -> cartService.addProduct(200L, 100L, 5));
    }

    // -------------------------
    // removeProduct
    // -------------------------

    @Test
    void testRemoveProduct_RemovesItem() {
        CartItem item = new CartItem(product, 1);
        item.setCart(cart);
        cart.getItems().add(item);

        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);

        Cart result = cartService.removeProduct(200L, 100L);

        assertTrue(result.getItems().isEmpty());
        verify(cartDAO).update(cart);
    }

    @Test
    void testRemoveProduct_ProductNotFound() {
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);

        assertThrows(IllegalArgumentException.class, () -> cartService.removeProduct(200L, 999L));
    }

    // -------------------------
    // decrementProductQuantity
    // -------------------------

    @Test
    void testDecrementProductQuantity_Decrements() {
        CartItem item = new CartItem(product, 3);
        item.setCart(cart);
        cart.getItems().add(item);

        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);

        Cart result = cartService.decrementProductQuantity(200L, 100L);

        assertEquals(2, result.getItems().get(0).getQuantity());
        verify(cartDAO).update(cart);
    }

    @Test
    void testDecrementProductQuantity_RemovesWhenQuantityOne() {
        CartItem item = new CartItem(product, 1);
        item.setCart(cart);
        cart.getItems().add(item);

        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);

        Cart result = cartService.decrementProductQuantity(200L, 100L);

        assertTrue(result.getItems().isEmpty());
        verify(cartDAO).update(cart);
    }

    @Test
    void testDecrementProductQuantity_ProductNotFound() {
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);

        assertThrows(IllegalArgumentException.class, () -> cartService.decrementProductQuantity(200L, 100L));
    }

    // -------------------------
    // clearCart
    // -------------------------

    @Test
    void testClearCart() {
        cart.getItems().add(new CartItem(product, 2));
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);

        Cart result = cartService.clearCart(200L);

        assertTrue(result.getItems().isEmpty());
        verify(cartDAO).update(cart);
    }

    // -------------------------
    // totals
    // -------------------------

    @Test
    void testGetTotalAndVAT() {
        CartItem item = new CartItem(product, 2); // 2 x 1000
        item.setCart(cart);
        cart.getItems().add(item);

        BigDecimal total = cartService.getTotal(cart);
        BigDecimal totalWithVAT = cartService.getTotalWithVAT(cart);

        assertEquals(new BigDecimal("2000"), total);
        assertEquals(new BigDecimal("2300.00"), totalWithVAT); // 15% VAT
    }

    // -------------------------
    // expireCarts
    // -------------------------

    @Test
    void testExpireCarts_SetsExpired() {
        cart.setExpiresAt(LocalDateTime.now().minusMinutes(10));
        List<Cart> expired = List.of(cart);

        when(cartDAO.findByStatusAndExpiresBefore(eq(CartStatus.NEW), any(LocalDateTime.class)))
                .thenReturn(expired);

        cartService.expireCarts();

        assertEquals(CartStatus.EXPIRED, cart.getStatus());
        verify(cartDAO).updateAll(expired);
    }

    @Test
    void testExpireCarts_NoExpired() {
        when(cartDAO.findByStatusAndExpiresBefore(eq(CartStatus.NEW), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        cartService.expireCarts();

        verify(cartDAO, never()).updateAll(anyList());
    }
}


