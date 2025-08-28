package com.example.services;

import com.example.dao.ProductDAO;
import com.example.dto.PaginatedResponse;
import com.example.dto.ProductRequest;
import com.example.dto.ProductResponse;
import com.example.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductDAO productDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProducts_withFilters() {
        // Arrange
        Product product1 = new Product("Laptop", "High-end laptop", new BigDecimal("1200.00"), "SKU123", 10);
        Product product2 = new Product("Mouse", "Wireless mouse", new BigDecimal("25.00"), "SKU124", 50);

        List<Product> products = Arrays.asList(product1, product2);
        when(productDAO.findProducts(0, 10, "Lap", new BigDecimal("1000"), new BigDecimal("1300"), true))
                .thenReturn(products);
        when(productDAO.countProducts("Lap", new BigDecimal("1000"), new BigDecimal("1300"), true))
                .thenReturn(1L);

        // Act
        PaginatedResponse<ProductResponse> response =
                productService.getProducts(1, 10, "Lap", new BigDecimal("1000"), new BigDecimal("1300"), true);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getItems().size());
        assertEquals(1, response.getTotalItems());
        assertEquals(1, response.getCurrentPage());
        verify(productDAO, times(1)).findProducts(0, 10, "Lap", new BigDecimal("1000"), new BigDecimal("1300"), true);
        verify(productDAO, times(1)).countProducts("Lap", new BigDecimal("1000"), new BigDecimal("1300"), true);
    }

    @Test
    void testGetProductById_found() {
        Product product = new Product("Laptop", "High-end laptop", new BigDecimal("1200.00"), "SKU123", 10);
        when(productDAO.findById(Product.class, 1L)).thenReturn(product);

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals("Laptop", response.getName());
    }

    @Test
    void testGetProductById_notFound() {
        when(productDAO.findById(Product.class, 1L)).thenReturn(null);
        ProductResponse response = productService.getProductById(1L);
        assertNull(response);
    }

    @Test
    void testCreateProduct() {
        ProductRequest request = new ProductRequest("Laptop", "High-end laptop", new BigDecimal("1200.00"), "SKU123", 10);

        Product createdProduct = new Product(request.getName(), request.getDescription(), request.getPrice(), request.getProductCode(), request.getStock());
        doNothing().when(productDAO).save(any(Product.class));

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals(request.getName(), response.getName());
        verify(productDAO, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_found() {
        Product existing = new Product("Old Laptop", "Old description", new BigDecimal("1000.00"), "SKU000", 5);
        when(productDAO.findById(Product.class, 1L)).thenReturn(existing);

        ProductRequest request = new ProductRequest("Laptop", "Updated description", new BigDecimal("1200.00"), "SKU123", 10);

        ProductResponse response = productService.updateProduct(1L, request);

        assertNotNull(response);
        assertEquals("Laptop", response.getName());
        assertEquals("Updated description", response.getDescription());
        verify(productDAO, times(1)).update(existing);
    }

    @Test
    void testUpdateProduct_notFound() {
        when(productDAO.findById(Product.class, 1L)).thenReturn(null);

        ProductRequest request = new ProductRequest("Laptop", "Updated description", new BigDecimal("1200.00"), "SKU123", 10);
        ProductResponse response = productService.updateProduct(1L, request);

        assertNull(response);
        verify(productDAO, never()).update(any());
    }

    @Test
    void testDeleteProduct_found() {
        Product existing = new Product("Laptop", "High-end laptop", new BigDecimal("1200.00"), "SKU123", 10);
        when(productDAO.findById(Product.class, 1L)).thenReturn(existing);
        doNothing().when(productDAO).delete(existing);

        boolean result = productService.deleteProduct(1L);
        assertTrue(result);
        verify(productDAO, times(1)).delete(existing);
    }

    @Test
    void testDeleteProduct_notFound() {
        when(productDAO.findById(Product.class, 1L)).thenReturn(null);
        boolean result = productService.deleteProduct(1L);
        assertFalse(result);
        verify(productDAO, never()).delete(any());
    }
}

