package com.example.dao;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public abstract class BaseDAO<T> {

    @PersistenceContext(unitName = "ProductPU")
    protected EntityManager em;
    
    public EntityManager getEntityManager() {
        return em;
    }
    
    public T findById(Class<T> entityClass, Object id) {
        return em.find(entityClass, id);
    }
    
    public T findById(Object id) {
        return find(id);
    }
    
    public void save(T entity) {
        em.persist(entity);
    }

    private final Class<T> entityClass;

    protected BaseDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void create(T entity) {
        em.persist(entity);
    }

    public T find(Object id) {
        return em.find(entityClass, id);
    }
    
    public List<T> findAll(Class<T> clazz) {
        return em.createQuery("SELECT e FROM " + clazz.getSimpleName() + " e", clazz)
                 .getResultList();
    }

    public void update(T entity) {
        em.merge(entity);
    }

    public void delete(T entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    public List<T> findAll() {
        return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                 .getResultList();
    }
}

