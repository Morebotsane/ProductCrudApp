package com.example.services;

import com.example.dao.ProductDAO;
import com.example.dto.PaginatedResponse;
import com.example.dto.ProductRequest;
import com.example.dto.ProductResponse;
import com.example.entities.Product;
import com.example.security.JwtTokenService;

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

    @Inject
    private JwtTokenService jwtService;

    // -------------------------
    // LIST PRODUCTS
    // -------------------------
    public PaginatedResponse<ProductResponse> getProducts(
            int page,
            int size,
            String nameFilter,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock
    ) {
        enforceAuthenticated();

        int offset = Math.max(0, (page - 1) * size);
        List<Product> products = productDAO.findProducts(offset, size, nameFilter, minPrice, maxPrice, inStock);

        List<ProductResponse> dtoList = products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());

        long totalItems = productDAO.countProducts(nameFilter, minPrice, maxPrice, inStock);

        return new PaginatedResponse<>(dtoList, totalItems, page, size);
    }

    // -------------------------
    // GET PRODUCT BY ID
    // -------------------------
    public ProductResponse getProductById(Long id) {
        enforceAuthenticated();

        Product product = productDAO.findById(Product.class, id);
        return (product != null) ? ProductResponse.fromEntity(product) : null;
    }

    // -------------------------
    // CREATE PRODUCT
    // -------------------------
    public ProductResponse createProduct(ProductRequest request) {
        enforceAdminOrSuper();

        Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getProductCode(),
                request.getStock()
        );

        productDAO.save(product);
        productDAO.getEntityManager().flush();

        audit("CREATE_PRODUCT", product.getId(),
                String.format("{\"name\":\"%s\",\"price\":%s}", request.getName(), request.getPrice()));

        return ProductResponse.fromEntity(product);
    }

    // -------------------------
    // UPDATE PRODUCT
    // -------------------------
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        enforceAdminOrSuper();

        Product existing = productDAO.findById(Product.class, id);
        if (existing == null) return null;

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());

        productDAO.update(existing);
        productDAO.getEntityManager().flush();

        audit("UPDATE_PRODUCT", existing.getId(),
                String.format("{\"name\":\"%s\",\"price\":%s}", request.getName(), request.getPrice()));

        return ProductResponse.fromEntity(existing);
    }

    // -------------------------
    // DELETE PRODUCT
    // -------------------------
    public boolean deleteProduct(Long id) {
        enforceAdminOrSuper();

        Product existing = productDAO.findById(Product.class, id);
        if (existing == null) return false;

        productDAO.delete(existing);
        productDAO.getEntityManager().flush();

        audit("DELETE_PRODUCT", id, "{}");

        return true;
    }

    // -------------------------
    // Role enforcement
    // -------------------------
    private void enforceAdminOrSuper() {
        if (!jwtService.isAdmin() && !jwtService.hasRole("ROLE_SUPER")) {
            throw new SecurityException("Forbidden: only admins or super users can perform this action");
        }
    }

    private void enforceAuthenticated() {
        if (!jwtService.isAdmin() && !jwtService.hasRole("ROLE_SUPER") && !jwtService.isCustomer()) {
            throw new SecurityException("Forbidden: authentication required");
        }
    }

    // -------------------------
    // Audit helper
    // -------------------------
    private void audit(String action, Long entityId, String payload) {
        String actor = jwtService.getUsername();
        auditService.record(actor, action, "Product", entityId, payload);
    }
}
