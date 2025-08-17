package com.example.dao;

import com.example.entities.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class ProductDAO extends BaseDAO<Product> {

    public ProductDAO() {
        super(Product.class);
    }

    public List<Product> findProducts(int offset, int limit, String nameFilter) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT p FROM Product p";
            if (nameFilter != null && !nameFilter.isEmpty()) {
                jpql += " WHERE LOWER(p.name) LIKE LOWER(:name)";
            }

            TypedQuery<Product> query = em.createQuery(jpql, Product.class);

            if (nameFilter != null && !nameFilter.isEmpty()) {
                query.setParameter("name", "%" + nameFilter + "%");
            }

            query.setFirstResult(offset);
            query.setMaxResults(limit);

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long countProducts(String nameFilter) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(p) FROM Product p";
            if (nameFilter != null && !nameFilter.isEmpty()) {
                jpql += " WHERE LOWER(p.name) LIKE LOWER(:name)";
            }

            TypedQuery<Long> query = em.createQuery(jpql, Long.class);

            if (nameFilter != null && !nameFilter.isEmpty()) {
                query.setParameter("name", "%" + nameFilter + "%");
            }

            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
