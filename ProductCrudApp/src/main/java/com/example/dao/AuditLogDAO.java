package com.example.dao;

import com.example.entities.AuditLog;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class AuditLogDAO extends BaseDAO<AuditLog> {

    public AuditLogDAO() {
        super(AuditLog.class);
    }

    // -------------------------
    // Find logs for a specific entity
    // -------------------------
    public List<AuditLog> findByEntity(String entityType, Long entityId) {
        TypedQuery<AuditLog> query = getEntityManager().createQuery(
            "SELECT a FROM AuditLog a WHERE a.entityType = :type AND a.entityId = :id ORDER BY a.at DESC",
            AuditLog.class
        );
        query.setParameter("type", entityType);
        query.setParameter("id", entityId);
        return query.getResultList();
    }

    // -------------------------
    // Find logs by entity type (paged)
    // -------------------------
    public List<AuditLog> findByType(String entityType, int page, int size) {
        TypedQuery<AuditLog> query = getEntityManager().createQuery(
            "SELECT a FROM AuditLog a WHERE a.entityType = :type ORDER BY a.at DESC",
            AuditLog.class
        );
        query.setParameter("type", entityType);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    // -------------------------
    // Fetch ALL logs (paged)
    // -------------------------
    public List<AuditLog> findAllPaged(int page, int size) {
        TypedQuery<AuditLog> query = getEntityManager().createQuery(
            "SELECT a FROM AuditLog a ORDER BY a.at DESC",
            AuditLog.class
        );
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        return query.getResultList();
    }
}
