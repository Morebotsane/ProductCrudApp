package com.example.dao;

import com.example.entities.Product;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.List;

@Stateless
public class ProductDAO extends BaseDAO<Product> {

    // EJB requires a no-arg constructor
    public ProductDAO() {
        super(Product.class);
    }

    /**
     * Find products with pagination and optional filters.
     */
    public List<Product> findProducts(int offset, int limit, String nameFilter,
                                      BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock) {

        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");

        if (nameFilter != null && !nameFilter.isEmpty()) {
            jpql.append(" AND LOWER(p.name) LIKE :name");
        }
        if (minPrice != null) {
            jpql.append(" AND p.price >= :minPrice");
        }
        if (maxPrice != null) {
            jpql.append(" AND p.price <= :maxPrice");
        }
        if (Boolean.TRUE.equals(inStock)) {
            jpql.append(" AND p.stock > 0");
        }

        jpql.append(" ORDER BY p.createdAt DESC");

        TypedQuery<Product> query = getEntityManager().createQuery(jpql.toString(), Product.class);

        if (nameFilter != null && !nameFilter.isEmpty()) {
            query.setParameter("name", "%" + nameFilter.toLowerCase() + "%");
        }
        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }

        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    /**
     * Count products with optional filters (for pagination metadata)
     */
    public long countProducts(String nameFilter, BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock) {

        StringBuilder jpql = new StringBuilder("SELECT COUNT(p) FROM Product p WHERE 1=1");

        if (nameFilter != null && !nameFilter.isEmpty()) {
            jpql.append(" AND LOWER(p.name) LIKE :name");
        }
        if (minPrice != null) {
            jpql.append(" AND p.price >= :minPrice");
        }
        if (maxPrice != null) {
            jpql.append(" AND p.price <= :maxPrice");
        }
        if (Boolean.TRUE.equals(inStock)) {
            jpql.append(" AND p.stock > 0");
        }

        TypedQuery<Long> query = getEntityManager().createQuery(jpql.toString(), Long.class);

        if (nameFilter != null && !nameFilter.isEmpty()) {
            query.setParameter("name", "%" + nameFilter.toLowerCase() + "%");
        }
        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }

        return query.getSingleResult();
    }
}