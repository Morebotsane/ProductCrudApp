package com.example.services;

import com.example.dao.AuditLogDAO;
import com.example.entities.AuditLog;
import com.example.logging.LogKeys;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class AuditService {

    @Inject
    private AuditLogDAO auditLogDAO;

    public AuditService() {}

    // -------------------------
    // Record a new log entry
    // -------------------------
    public void record(String actor, String action, String entityType, Long entityId, String payload) {
        AuditLog log = new AuditLog(
                actor,
                action,
                entityType,
                entityId,
                payload,
                LocalDateTime.now()
        );

        System.out.println(">>> Persisting audit: " + log.getAction() + " for entityId=" + entityId);
        auditLogDAO.save(log);
    }

    // -------------------------
    // Utility to set audit properties into request context
    // -------------------------
    public void setAudit(ContainerRequestContext ctx, String entityType, String action, Long entityId, String payload) {
        ctx.setProperty("entityType", entityType);
        if (entityId != null) ctx.setProperty("entityId", entityId);
        ctx.setProperty("auditPayload", (payload != null) ? payload : "{}");

        // Ensure actor defaults to system if missing
        String customerId = MDC.get(LogKeys.CUSTOMER_ID);
        String actor = (customerId != null) ? "customer:" + customerId : "system";
        ctx.setProperty("auditActor", actor);
        ctx.setProperty("auditAction", action);
    }

    // -------------------------
    // Fetch logs for a specific entity
    // -------------------------
    public List<AuditLog> getLogs(String entityType, Long entityId) {
        return auditLogDAO.findByEntity(entityType, entityId);
    }

    // -------------------------
    // Fetch logs by entity type (with paging)
    // -------------------------
    public List<AuditLog> getLogsByType(String entityType, int page, int size) {
        return auditLogDAO.findByType(entityType, page, size);
    }
}