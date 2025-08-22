package com.example.dao;

import com.example.entities.Product;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class ProductDAO extends BaseDAO<Product> {

    public ProductDAO() {
        super(Product.class);
    }

    public List<Product> findProducts(int offset, int limit, String nameFilter) {
        String jpql = "SELECT p FROM Product p";
        if (nameFilter != null && !nameFilter.isEmpty()) {
            jpql += " WHERE LOWER(p.name) LIKE LOWER(:name)";
        }

        TypedQuery<Product> query = getEntityManager().createQuery(jpql, Product.class);

        if (nameFilter != null && !nameFilter.isEmpty()) {
            query.setParameter("name", "%" + nameFilter + "%");
        }

        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList(); // container manages EM lifecycle
    }

    public long countProducts(String nameFilter) {
        String jpql = "SELECT COUNT(p) FROM Product p";
        if (nameFilter != null && !nameFilter.isEmpty()) {
            jpql += " WHERE LOWER(p.name) LIKE LOWER(:name)";
        }

        TypedQuery<Long> query = getEntityManager().createQuery(jpql, Long.class);

        if (nameFilter != null && !nameFilter.isEmpty()) {
            query.setParameter("name", "%" + nameFilter + "%");
        }

        return query.getSingleResult(); // container manages EM lifecycle
    }
}
