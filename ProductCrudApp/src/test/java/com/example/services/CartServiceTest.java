package com.example.services;

import com.example.dao.CartDAO;
import com.example.dao.CustomerDAO;
import com.example.dao.ProductDAO;
import com.example.entities.*;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock private CartDAO cartDAO;
    @Mock private ProductDAO productDAO;
    @Mock private CustomerDAO customerDAO;
    @Mock private AuditService auditService;
    @Mock private SecurityContext securityContext;
    @Mock private JsonWebToken jwt;
    @Mock private EntityManager entityManager;

    private Customer customer;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(cartDAO.getEntityManager()).thenReturn(entityManager);
        doNothing().when(entityManager).flush();

        // Sample Customer
        customer = new Customer();
        customer.setId(1L);

        // Sample Product
        product = new Product();
        product.setId(100L);
        product.setName("Laptop");
        product.setStock(10);
        product.setPrice(new BigDecimal("1000"));

        // Sample Cart
        cart = new Cart();
        cart.setId(200L);
        cart.setCustomer(customer);
        cart.setStatus(CartStatus.NEW);
        cart.setItems(new ArrayList<>());
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        cart.setExpiresAt(LocalDateTime.now().plusHours(2));

        when(jwt.getName()).thenReturn("system");
    }

    // -------------------------
    // getOrCreateActiveCart
    // -------------------------
    @Test
    void testGetOrCreateActiveCart_AdminCanCreateAnyCart() {
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);
        when(cartDAO.findActiveCartByCustomerId(1L)).thenReturn(null);

        // Add this line to avoid NumberFormatException
        when(jwt.getSubject()).thenReturn("1");

        Cart result = cartService.getOrCreateActiveCart(1L);

        assertEquals(CartStatus.NEW, result.getStatus());
        verify(cartDAO).save(any());
        verify(entityManager).flush();
        verify(auditService).record(eq("system"), eq("CREATE_CART"), eq("Cart"), any(), contains("\"customerId\": 1"));
    }

    @Test
    void testGetOrCreateActiveCart_CustomerCanCreateOwnCart() {
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
        when(jwt.getSubject()).thenReturn("1");
        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);
        when(cartDAO.findActiveCartByCustomerId(1L)).thenReturn(null);

        Cart result = cartService.getOrCreateActiveCart(1L);

        assertEquals(CartStatus.NEW, result.getStatus());
    }

    @Test
    void testGetOrCreateActiveCart_CustomerCannotCreateOthersCart() {
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
        when(jwt.getSubject()).thenReturn("2"); // different customer

        SecurityException ex = assertThrows(SecurityException.class, () -> cartService.getOrCreateActiveCart(1L));
        assertEquals("Customers can only create their own carts", ex.getMessage());
    }

    // -------------------------
    // addProduct
    // -------------------------
    @Test
    void testAddProduct_AdminCanAddAnyCart() {
        cart.getItems().clear();
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 100L)).thenReturn(product);

        Cart result = cartService.addProduct(200L, 100L, 2);

        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getItems().get(0).getQuantity());
        verify(auditService).record(eq("system"), eq("ADD_PRODUCT_TO_CART"), eq("Cart"), eq(200L), anyString());
    }

    @Test
    void testAddProduct_CustomerCannotAddOthersCart() {
        when(securityContext.isUserInRole("ROLE_CUSTOMER")).thenReturn(true);
        when(jwt.getSubject()).thenReturn("2"); // different customer
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);

        SecurityException ex = assertThrows(SecurityException.class, () -> cartService.addProduct(200L, 100L, 1));
        assertEquals("Forbidden: Cannot access another customer's cart", ex.getMessage());
    }

    @Test
    void testAddProduct_IncrementExistingQuantity() {
        cart.getItems().add(new CartItem(product, 3));
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 100L)).thenReturn(product);

        Cart result = cartService.addProduct(200L, 100L, 2);

        assertEquals(5, result.getItems().get(0).getQuantity());
    }

    @Test
    void testAddProduct_InsufficientStockThrows() {
        product.setStock(1);
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 100L)).thenReturn(product);

        assertThrows(IllegalArgumentException.class, () -> cartService.addProduct(200L, 100L, 5));
    }

    // -------------------------
    // removeProduct
    // -------------------------
    @Test
    void testRemoveProduct_AdminCanRemove() {
        CartItem item = new CartItem(product, 2);
        item.setCart(cart);
        cart.getItems().add(item);

        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);

        Cart result = cartService.removeProduct(200L, 100L);

        assertTrue(result.getItems().isEmpty());
        verify(auditService).record(eq("system"), eq("REMOVE_PRODUCT_FROM_CART"), eq("Cart"), eq(200L), anyString());
    }

    // -------------------------
    // decrementProductQuantity
    // -------------------------
    @Test
    void testDecrementProductQuantity_DecrementsQuantity() {
        CartItem item = new CartItem(product, 3);
        item.setCart(cart);
        cart.getItems().add(item);

        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);

        Cart result = cartService.decrementProductQuantity(200L, 100L);

        assertEquals(2, result.getItems().get(0).getQuantity());
    }

    @Test
    void testDecrementProductQuantity_RemovesWhenOne() {
        CartItem item = new CartItem(product, 1);
        item.setCart(cart);
        cart.getItems().add(item);

        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);

        Cart result = cartService.decrementProductQuantity(200L, 100L);
        assertTrue(result.getItems().isEmpty());
    }

    // -------------------------
    // clearCart
    // -------------------------
    @Test
    void testClearCart_AdminCanClear() {
        cart.getItems().add(new CartItem(product, 2));
        when(securityContext.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        when(cartDAO.findById(Cart.class, 200L)).thenReturn(cart);

        Cart result = cartService.clearCart(200L);

        assertTrue(result.getItems().isEmpty());
        verify(auditService).record(eq("system"), eq("CLEAR_CART"), eq("Cart"), eq(200L), eq("{}"));
    }

    // -------------------------
    // totals
    // -------------------------
    @Test
    void testGetTotalAndVAT() {
        CartItem item = new CartItem(product, 2);
        item.setCart(cart);
        cart.getItems().add(item);

        BigDecimal total = cartService.getTotal(cart);
        BigDecimal totalWithVAT = cartService.getTotalWithVAT(cart);

        assertEquals(new BigDecimal("2000"), total);
        assertEquals(new BigDecimal("2300.00"), totalWithVAT);
    }

    // -------------------------
    // expireCarts
    // -------------------------
    @Test
    void testExpireCarts() {
        cart.setExpiresAt(LocalDateTime.now().minusMinutes(10));
        List<Cart> expired = List.of(cart);

        when(cartDAO.findByStatusAndExpiresBefore(eq(CartStatus.NEW), any(LocalDateTime.class)))
                .thenReturn(expired);

        cartService.expireCarts();

        assertEquals(CartStatus.EXPIRED, cart.getStatus());
        verify(cartDAO).updateAll(expired);
        verify(auditService).record(eq("system"), eq("EXPIRE_CART"), eq("Cart"), eq(200L), eq("{}"));
    }
}
