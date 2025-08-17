package com.example.services;

import com.example.dao.ProductDAO;
import com.example.dto.PaginatedResponse;
import com.example.dto.ProductRequest;
import com.example.dto.ProductResponse;
import com.example.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
//import java.util.Arrays;
import java.util.Collections;
//import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = new Product("Laptop", "Gaming Laptop", new BigDecimal("1200.00"));
        product.setId(1L);
    }

    @Test
    void testGetProducts_withResults() {
        when(productDAO.findProducts(0, 10, "Laptop")).thenReturn(Collections.singletonList(product));
        when(productDAO.countProducts("Laptop")).thenReturn(1L);

        PaginatedResponse<ProductResponse> response = productService.getProducts(0, 10, "Laptop");

        assertEquals(1, response.getItems().size());
        assertEquals(1L, response.getTotalItems());
        assertEquals("Laptop", response.getItems().get(0).getName());

        verify(productDAO).findProducts(0, 10, "Laptop");
        verify(productDAO).countProducts("Laptop");
    }

    @Test
    void testGetProductById_found() {
        when(productDAO.findById(Product.class, 1L)).thenReturn(product);

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals("Laptop", response.getName());
    }

    @Test
    void testGetProductById_notFound() {
        when(productDAO.findById(Product.class, 99L)).thenReturn(null);

        ProductResponse response = productService.getProductById(99L);

        assertNull(response);
    }

    @Test
    void testCreateProduct() {
        ProductRequest request = new ProductRequest("Phone", "Smartphone", new BigDecimal("800.00"));
        Product newProduct = new Product(request.getName(), request.getDescription(), request.getPrice());
        newProduct.setId(2L);

        // Mock save: void, so we just doNothing
        doNothing().when(productDAO).save(any(Product.class));

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("Phone", response.getName());

        verify(productDAO).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_found() {
        when(productDAO.findById(Product.class, 1L)).thenReturn(product);

        ProductRequest updateRequest = new ProductRequest("Updated Laptop", "Better GPU", new BigDecimal("1500.00"));
        doNothing().when(productDAO).update(product);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Laptop", response.getName());
        assertEquals("Better GPU", response.getDescription());

        verify(productDAO).update(product);
    }

    @Test
    void testUpdateProduct_notFound() {
        when(productDAO.findById(Product.class, 99L)).thenReturn(null);

        ProductRequest updateRequest = new ProductRequest("Nope", "Doesn't exist", new BigDecimal("999.00"));

        ProductResponse response = productService.updateProduct(99L, updateRequest);

        assertNull(response);
    }

    @Test
    void testDeleteProduct_found() {
        when(productDAO.findById(Product.class, 1L)).thenReturn(product);
        doNothing().when(productDAO).delete(product);

        boolean deleted = productService.deleteProduct(1L);

        assertTrue(deleted);
        verify(productDAO).delete(product);
    }

    @Test
    void testDeleteProduct_notFound() {
        when(productDAO.findById(Product.class, 99L)).thenReturn(null);

        boolean deleted = productService.deleteProduct(99L);

        assertFalse(deleted);
        verify(productDAO, never()).delete(any(Product.class));
    }
}
