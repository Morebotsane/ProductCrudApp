package com.example.services;

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

    /**
     * Get products with optional filters and pagination
     */
    public PaginatedResponse<ProductResponse> getProducts(
            int page,
            int size,
            String nameFilter,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock
    ) {
        int offset = (page - 1) * size;

        // Fetch filtered and paginated products from DAO
        List<Product> products = productDAO.findProducts(offset, size, nameFilter, minPrice, maxPrice, inStock);

        // Map entity list to DTO list
        List<ProductResponse> dtoList = products.stream()
                                                .map(ProductResponse::fromEntity)
                                                .collect(Collectors.toList());

        // Count total items for pagination metadata
        long totalItems = productDAO.countProducts(nameFilter, minPrice, maxPrice, inStock);

        // Compute current page index (0-based)
        int currentPage = page;

        return new PaginatedResponse<>(dtoList, totalItems, currentPage, size);
    }

    /** Retrieve single product as DTO */
    public ProductResponse getProductById(Long id) {
        Product product = productDAO.findById(Product.class, id);
        if (product == null) return null;
        return ProductResponse.fromEntity(product);
    }

    /** Create a new product from DTO */
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product(request.getName(), request.getDescription(), request.getPrice(), request.getProductCode(), request.getStock());
        productDAO.save(product);
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

        return ProductResponse.fromEntity(existing);
    }

    /** Delete product by ID */
    public boolean deleteProduct(Long id) {
        Product existing = productDAO.findById(Product.class, id);
        if (existing != null) {
            productDAO.delete(existing);
            return true;
        }
        return false;
    }
}