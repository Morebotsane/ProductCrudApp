package com.example.services;

import com.example.dao.ProductDAO;
import com.example.dto.PaginatedResponse;
import com.example.dto.ProductRequest;
import com.example.dto.ProductResponse;
import com.example.entities.Product;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class ProductService {

    @Inject
    private ProductDAO productDAO;

    /**
     * Retrieve paginated and optionally filtered products.
     *
     * @param offset     starting index
     * @param limit      number of products to retrieve
     * @param nameFilter optional name filter (case-insensitive)
     * @return PaginatedResponse of ProductResponse
     */
    public PaginatedResponse<ProductResponse> getProducts(int offset, int limit, String nameFilter) {
        // Fetch filtered and paginated products from DAO
        List<Product> products = productDAO.findProducts(offset, limit, nameFilter);

        // Map entity list to DTO list
        List<ProductResponse> dtoList = products.stream()
                								.map(ProductResponse::fromEntity)
                								.collect(Collectors.toList());

        // Count total items for pagination metadata
        long totalItems = productDAO.countProducts(nameFilter);

        // Compute current page index (0-based)
        int currentPage = offset / limit;

        // Return generic PaginatedResponse
        return new PaginatedResponse<>(dtoList, totalItems, currentPage, limit);
    }

    /** Retrieve single product as DTO */
    public ProductResponse getProductById(Long id) {
        Product product = productDAO.findById(Product.class, id);
        if (product == null) return null;
        return ProductResponse.fromEntity(product);
    }

    /** Create a new product from DTO */
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product(request.getName(), request.getDescription(), request.getPrice());
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
