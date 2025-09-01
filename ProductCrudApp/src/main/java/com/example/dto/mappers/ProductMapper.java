package com.example.dto.mappers;

import com.example.dto.ProductRequest;
import com.example.dto.ProductResponse;
import com.example.entities.Product;

public class ProductMapper {

    /** Convert Product entity to DTO */
    public static ProductResponse toResponse(Product product) {
        if (product == null) return null;

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setProductCode(product.getProductCode());
        response.setStock(product.getStock());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    /** Convert ProductRequest DTO to new Product entity */
    public static Product toEntity(ProductRequest request) {
        if (request == null) return null;

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setProductCode(request.getProductCode());
        product.setStock(request.getStock());
        return product;
    }

    /** Update an existing Product entity from ProductRequest DTO */
    public static void updateEntity(Product product, ProductRequest request) {
        if (product == null || request == null) return;

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setProductCode(request.getProductCode());
        product.setStock(request.getStock());
    }
}
