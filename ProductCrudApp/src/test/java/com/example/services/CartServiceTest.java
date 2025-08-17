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

public class CartServiceTest {

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

    // Create cart for existing customer
    @Test
    public void testCreateCartForCustomer_Success() {
        Customer customer = new Customer();
        customer.setId(1L);

        when(customerDAO.findById(Customer.class, 1L)).thenReturn(customer);

        Cart createdCart = cartService.createCartForCustomer(1L);

        assertNotNull(createdCart);
        assertEquals(customer, createdCart.getCustomer());
        verify(cartDAO).save(createdCart);
    }

    // Create cart for non-existent customer
    @Test
    public void testCreateCartForCustomer_CustomerNotFound_Throws() {
        when(customerDAO.findById(Customer.class, 99L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cartService.createCartForCustomer(99L);
        });

        assertEquals("Customer not found", ex.getMessage());
    }

    @Test
    public void testAddProduct_NewProduct_Success() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.00"));

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 1L)).thenReturn(product);

        Cart updatedCart = cartService.addProduct(1L, 1L, 2);

        assertEquals(1, updatedCart.getItems().size());
        assertEquals(2, updatedCart.getItems().get(0).getQuantity());
        verify(cartDAO).update(cart);
    }

    @Test
    public void testAddProduct_ExistingProduct_IncreasesQuantity() {
        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.00"));

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
    public void testAddProduct_CartNotFound_Throws() {
        when(cartDAO.findById(Cart.class, 99L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cartService.addProduct(99L, 1L, 1);
        });

        assertEquals("Cart not found", ex.getMessage());
    }

    @Test
    public void testAddProduct_ProductNotFound_Throws() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 99L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cartService.addProduct(1L, 99L, 1);
        });

        assertEquals("Product not found", ex.getMessage());
    }

    @Test
    public void testAddProduct_InvalidQuantity_Throws() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.00"));

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);
        when(productDAO.findById(Product.class, 1L)).thenReturn(product);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cartService.addProduct(1L, 1L, 0);
        });
        assertEquals("Quantity must be greater than zero", ex.getMessage());
    }

    @Test
    public void testRemoveProduct_Success() {
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
    public void testRemoveProduct_ProductNotFound_Throws() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cartService.removeProduct(1L, 1L);
        });

        assertEquals("Product not found in cart", ex.getMessage());
    }

    @Test
    public void testGetTotal() {
        Product product1 = new Product();
        product1.setPrice(new BigDecimal("10.00"));

        Product product2 = new Product();
        product2.setPrice(new BigDecimal("20.00"));

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
    public void testGetTotalWithVAT() {
        Product product = new Product();
        product.setPrice(new BigDecimal("100.00"));

        CartItem item = new CartItem(product, 1);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        BigDecimal expected = new BigDecimal("115.00");
        BigDecimal actual = cartService.getTotalWithVAT(1L);

        assertEquals(0, expected.compareTo(actual));
    }

    @Test
    public void testDecrementProductQuantity_DecreaseQuantity() {
        Product product = new Product();
        product.setId(1L);

        CartItem item = new CartItem(product, 3);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        Cart updatedCart = cartService.decrementProductQuantity(1L, 1L);

        assertEquals(2, updatedCart.getItems().get(0).getQuantity());
    }

    @Test
    public void testDecrementProductQuantity_RemoveItem() {
        Product product = new Product();
        product.setId(1L);

        CartItem item = new CartItem(product, 1);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        Cart updatedCart = cartService.decrementProductQuantity(1L, 1L);

        assertTrue(updatedCart.getItems().isEmpty());
    }

    @Test
    public void testDecrementProductQuantity_ProductNotFound_Throws() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(cartDAO.findById(Cart.class, 1L)).thenReturn(cart);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cartService.decrementProductQuantity(1L, 1L);
        });

        assertEquals("Product not found in cart", ex.getMessage());
    }
}
