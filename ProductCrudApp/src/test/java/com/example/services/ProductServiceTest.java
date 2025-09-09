package com.example.services;

import com.example.dao.ProductDAO;
import com.example.dto.PaginatedResponse;
import com.example.dto.ProductRequest;
import com.example.dto.ProductResponse;
import com.example.entities.Product;

import jakarta.persistence.EntityManager;
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

    @Mock
    private AuditService auditService;

    @Mock
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(productDAO.getEntityManager()).thenReturn(entityManager);
        doNothing().when(entityManager).flush();
    }

    @Test
    void testGetProducts_withFilters() {
        Product p1 = new Product("Laptop", "High-end laptop", new BigDecimal("1200.00"), "SKU123", 10);
        Product p2 = new Product("Mouse", "Wireless mouse", new BigDecimal("25.00"), "SKU124", 50);
        List<Product> products = Arrays.asList(p1, p2);

        when(productDAO.findProducts(0, 10, "Lap", new BigDecimal("1000"), new BigDecimal("1300"), true))
                .thenReturn(products);
        when(productDAO.countProducts("Lap", new BigDecimal("1000"), new BigDecimal("1300"), true))
                .thenReturn(2L);

        PaginatedResponse<ProductResponse> response =
                productService.getProducts(1, 10, "Lap", new BigDecimal("1000"), new BigDecimal("1300"), true);

        assertNotNull(response);
        assertEquals(2, response.getItems().size());
        assertEquals(2, response.getTotalItems());
        assertEquals(1, response.getCurrentPage());

        verify(productDAO).findProducts(0, 10, "Lap", new BigDecimal("1000"), new BigDecimal("1300"), true);
        verify(productDAO).countProducts("Lap", new BigDecimal("1000"), new BigDecimal("1300"), true);
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

        doAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(101L);
            return null;
        }).when(productDAO).save(any(Product.class));

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Laptop", response.getName());

        verify(productDAO).save(any(Product.class));
        verify(auditService).record(
                eq("system"), eq("CREATE_PRODUCT"), eq("Product"), eq(101L), contains("Laptop")
        );
    }

    @Test
    void testUpdateProduct_found() {
        Product existing = new Product("Old Laptop", "Old description", new BigDecimal("1000.00"), "SKU000", 5);
        existing.setId(50L);
        when(productDAO.findById(Product.class, 50L)).thenReturn(existing);

        ProductRequest request = new ProductRequest("Laptop", "Updated description", new BigDecimal("1200.00"), "SKU123", 10);
        ProductResponse response = productService.updateProduct(50L, request);

        assertNotNull(response);
        assertEquals("Laptop", response.getName());
        assertEquals("Updated description", response.getDescription());

        verify(productDAO).update(existing);
        verify(auditService).record(eq("system"), eq("UPDATE_PRODUCT"), eq("Product"), eq(50L), contains("Laptop"));
    }

    @Test
    void testUpdateProduct_notFound() {
        when(productDAO.findById(Product.class, 999L)).thenReturn(null);

        ProductRequest request = new ProductRequest("Laptop", "Updated description", new BigDecimal("1200.00"), "SKU123", 10);
        ProductResponse response = productService.updateProduct(999L, request);

        assertNull(response);
        verify(productDAO, never()).update(any());
        verify(auditService, never()).record(anyString(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void testDeleteProduct_found() {
        Product existing = new Product("Laptop", "High-end laptop", new BigDecimal("1200.00"), "SKU123", 10);
        existing.setId(200L);
        when(productDAO.findById(Product.class, 200L)).thenReturn(existing);
        doNothing().when(productDAO).delete(existing);

        boolean result = productService.deleteProduct(200L);

        assertTrue(result);
        verify(productDAO).delete(existing);
        verify(auditService).record(eq("system"), eq("DELETE_PRODUCT"), eq("Product"), eq(200L), anyString());
    }

    @Test
    void testDeleteProduct_notFound() {
        when(productDAO.findById(Product.class, 999L)).thenReturn(null);

        boolean result = productService.deleteProduct(999L);

        assertFalse(result);
        verify(productDAO, never()).delete(any());
        verify(auditService, never()).record(anyString(), anyString(), anyString(), anyLong(), anyString());
    }
}
