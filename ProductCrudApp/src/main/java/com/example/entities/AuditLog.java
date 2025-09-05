package com.example.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_logs_seq")
    @SequenceGenerator(
        name = "audit_logs_seq",
        sequenceName = "audit_logs_id_seq", // matches your DB
        allocationSize = 1                   // critical for Postgres sequences
    )
    private Long id;

    @Column(name = "actor", nullable = false, updatable = false)
    private String actor;

    @Column(name = "action", nullable = false, updatable = false)
    private String action;

    @Column(name = "entity_type", nullable = false, updatable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false, updatable = false)
    private Long entityId;

    @Lob
    @Column(name = "payload", updatable = false)
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime CreatedAt;

    protected AuditLog() {} // JPA

    public AuditLog(String actor, String action, String entityType,
                    Long entityId, String payload, LocalDateTime CreatedAt) {
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.payload = payload;
        this.CreatedAt = CreatedAt;
    }

    public Long getId() { 
    	return id;
    }
    
    public String getActor() {
    	return actor; 
    }
    
    public String getAction() { 
    	return action;
    }
    
    public String getEntityType() { 
    	return entityType; 
    }
    
    public Long getEntityId() {
    	return entityId;
    }
    
    public String getPayload() {
    	return payload; 
    }
    
    public LocalDateTime getAt() {
    	return CreatedAt; 
    }

    @PreUpdate
    private void preventUpdate() { throw new IllegalStateException("AuditLog is immutable and cannot be updated"); }
    @PreRemove
    private void preventRemove() { throw new IllegalStateException("AuditLog is immutable and cannot be removed"); }
}
