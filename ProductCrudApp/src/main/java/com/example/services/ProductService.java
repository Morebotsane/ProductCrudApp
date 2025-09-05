package com.example.services;

//import com.example.services.AuditService;
import com.example.dao.ProductDAO;
import com.example.dto.PaginatedResponse;
import com.example.dto.ProductRequest;
import com.example.dto.ProductResponse;
import com.example.entities.Product;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class ProductService {

    @Inject
    private ProductDAO productDAO;

    @Inject
    private AuditService auditService;

    /** List products with optional filters and pagination */
    public PaginatedResponse<ProductResponse> getProducts(
            int page,
            int size,
            String nameFilter,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock
    ) {
        int offset = (page - 1) * size;

        List<Product> products = productDAO.findProducts(offset, size, nameFilter, minPrice, maxPrice, inStock);

        List<ProductResponse> dtoList = products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());

        long totalItems = productDAO.countProducts(nameFilter, minPrice, maxPrice, inStock);

        return new PaginatedResponse<>(dtoList, totalItems, page, size);
    }

    /** Retrieve single product as DTO */
    public ProductResponse getProductById(Long id) {
        Product product = productDAO.findById(Product.class, id);
        return (product != null) ? ProductResponse.fromEntity(product) : null;
    }

    /** Create a new product from DTO */
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getProductCode(),
                request.getStock()
        );

        productDAO.save(product);
        productDAO.getEntityManager().flush(); // force ID generation

        // Record audit after ID is assigned
        auditService.record(
                "system",
                "CREATE_PRODUCT",
                "Product",
                product.getId(),
                String.format("{\"name\":\"%s\",\"price\":%s}", request.getName(), request.getPrice())
        );

        return ProductResponse.fromEntity(product);
    }

    /** Update existing product by ID using DTO */
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existing = productDAO.findById(Product.class, id);
        if (existing == null) return null;

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());

        productDAO.update(existing);
        productDAO.getEntityManager().flush(); // ensure managed state

        // Record audit after update
        auditService.record(
                "system",
                "UPDATE_PRODUCT",
                "Product",
                existing.getId(),
                String.format("{\"name\":\"%s\",\"price\":%s}", request.getName(), request.getPrice())
        );

        return ProductResponse.fromEntity(existing);
    }

    /** Delete product by ID */
    public boolean deleteProduct(Long id) {
        Product existing = productDAO.findById(Product.class, id);
        if (existing == null) return false;

        productDAO.delete(existing);
        productDAO.getEntityManager().flush(); // optional but safe

        // Record audit after delete
        auditService.record(
                "system",
                "DELETE_PRODUCT",
                "Product",
                id,
                "{}"
        );

        return true;
    }
}
