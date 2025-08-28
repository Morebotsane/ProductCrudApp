package com.example.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.dao.CartDAO;
import com.example.dao.CustomerDAO;
import com.example.dao.ProductDAO;
import com.example.entities.Cart;
import com.example.entities.CartItem;
import com.example.entities.Customer;
import com.example.entities.Product;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CartServiceTest {

    @Mock
    private CartDAO cartDAO;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private CustomerDAO customerDAO;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // --- createCartForCustomer ---

    @Test
    void testCreateCartForCustomer_Success() {
        Customer customer = new Customer();
        customer.setId(1L);

        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);

        Cart cart = cartService.createCartForCustomer(1L);

        assertNotNull(cart);
        assertEquals(customer, cart.getCustomer());
        verify(cartDAO).save(cart);
    }

    @Test
    void testCreateCartForCustomer_CustomerNotFound_Throws() {
        when(customerDAO.findById(Customer.class, 99L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> cartService.createCartForCustomer(99L));

        assertEquals("Customer not found", ex.getMessage());
    }

    // --- addProduct ---

    @Test
    void testAddProduct_NewProduct_Success() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(10); // <-- fix stock

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 1L)).thenReturn(product);

        Cart updatedCart = cartService.addProduct(1L, 1L, 2);

        assertEquals(1, updatedCart.getItems().size());
        assertEquals(2, updatedCart.getItems().get(0).getQuantity());
        verify(cartDAO).update(cart);
    }

    @Test
    void testAddProduct_ExistingProduct_IncreasesQuantity() {
        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(10); // <-- fix stock

        CartItem existingItem = new CartItem(product, 2);

        Cart cart = new Cart();
        ArrayList<CartItem> items = new ArrayList<>();
        items.add(existingItem);
        cart.setItems(items);

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 1L)).thenReturn(product);

        Cart updatedCart = cartService.addProduct(1L, 1L, 3);

        assertEquals(5, updatedCart.getItems().get(0).getQuantity());
        verify(cartDAO).update(cart);
    }

    @Test
    void testAddProduct_CartNotFound_Throws() {
        when(cartDAO.findById(Cart.class, 99L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> cartService.addProduct(99L, 1L, 1));

        assertEquals("Cart not found", ex.getMessage());
    }

    @Test
    void testAddProduct_ProductNotFound_Throws() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 99L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> cartService.addProduct(1L, 99L, 1));

        assertEquals("Product not found", ex.getMessage());
    }

    @Test
    void testAddProduct_InvalidQuantity_Throws() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(10); // <-- fix stock

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 1L)).thenReturn(product);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> cartService.addProduct(1L, 1L, 0));

        assertEquals("Quantity must be greater than zero", ex.getMessage());
    }

    // --- removeProduct ---

    @Test
    void testRemoveProduct_Success() {
        Product product = new Product();
        product.setId(1L);

        CartItem item = new CartItem(product, 2);

        Cart cart = new Cart();
        ArrayList<CartItem> items = new ArrayList<>();
        items.add(item);
        cart.setItems(items);

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        Cart updatedCart = cartService.removeProduct(1L, 1L);

        assertTrue(updatedCart.getItems().isEmpty());
        verify(cartDAO).update(cart);
    }

    @Test
    void testRemoveProduct_ProductNotFound_Throws() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> cartService.removeProduct(1L, 1L));

        assertEquals("Product not found in cart", ex.getMessage());
    }

    // --- getTotal / getTotalWithVAT ---

    @Test
    void testGetTotal() {
        Product product1 = new Product();
        product1.setPrice(new BigDecimal("10.00"));
        product1.setStock(5); // fix stock

        Product product2 = new Product();
        product2.setPrice(new BigDecimal("20.00"));
        product2.setStock(5); // fix stock

        CartItem item1 = new CartItem(product1, 2);
        CartItem item2 = new CartItem(product2, 1);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item1);
        cart.getItems().add(item2);

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        BigDecimal total = cartService.getTotal(1L);

        assertEquals(new BigDecimal("40.00"), total);
    }

    @Test
    void testGetTotalWithVAT() {
        Product product = new Product();
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(5); // fix stock

        CartItem item = new CartItem(product, 1);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        BigDecimal expected = new BigDecimal("115.00"); // 100 + 15% VAT
        BigDecimal actual = cartService.getTotalWithVAT(1L);

        assertEquals(0, expected.compareTo(actual));
    }

    // --- decrementProductQuantity ---

    @Test
    void testDecrementProductQuantity_DecreaseQuantity() {
        Product product = new Product();
        product.setId(1L);
        product.setStock(5); // fix stock

        CartItem item = new CartItem(product, 3);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        Cart updatedCart = cartService.decrementProductQuantity(1L, 1L);

        assertEquals(2, updatedCart.getItems().get(0).getQuantity());
    }

    @Test
    void testDecrementProductQuantity_RemoveItem() {
        Product product = new Product();
        product.setId(1L);
        product.setStock(5); // fix stock

        CartItem item = new CartItem(product, 1);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        Cart updatedCart = cartService.decrementProductQuantity(1L, 1L);

        assertTrue(updatedCart.getItems().isEmpty());
    }

    @Test
    void testDecrementProductQuantity_ProductNotFound_Throws() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> cartService.decrementProductQuantity(1L, 1L));

        assertEquals("Product not found in cart", ex.getMessage());
    }
}

