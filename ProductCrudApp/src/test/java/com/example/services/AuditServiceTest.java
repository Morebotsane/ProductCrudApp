package com.example.services;

import com.example.dao.AuditLogDAO;
import com.example.entities.AuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditServiceTest {

    private AuditLogDAO dao;
    private AuditService service;

    @BeforeEach
    void setUp() {
        dao = mock(AuditLogDAO.class);

        // Use no-arg constructor (EJB style)
        service = new AuditService();

        // Manually inject the mock DAO (since we're outside the container)
        // Reflection hack: directly set the private field
        try {
            var field = AuditService.class.getDeclaredField("auditLogDAO");
            field.setAccessible(true);
            field.set(service, dao);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock DAO into AuditService", e);
        }
    }

    @Test
    void testRecordCreatesAuditLog() {
        service.record("system", "AUTO_CANCEL", "Order", 99L, "{ \"reason\": \"timeout\" }");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(dao).save(captor.capture());

        AuditLog log = captor.getValue();

        assertAll("AuditLog fields",
            () -> assertEquals("system", log.getActor()),
            () -> assertEquals("AUTO_CANCEL", log.getAction()),
            () -> assertEquals("Order", log.getEntityType()),
            () -> assertEquals(99L, log.getEntityId()),
            () -> assertTrue(log.getPayload().contains("timeout")),
            () -> assertNotNull(log.getAt())
        );
    }

    @Test
    void testRecordAllowsNullPayload() {
        service.record("user:42", "CREATE", "Cart", 1L, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(dao).save(captor.capture());

        AuditLog log = captor.getValue();
        assertNull(log.getPayload(), "Payload should be allowed to be null");
    }

    @Test
    void testGetLogsDelegatesToDao() {
        AuditLog mockLog = mock(AuditLog.class);
        when(mockLog.getActor()).thenReturn("tester");
        when(mockLog.getAt()).thenReturn(LocalDateTime.now());

        when(dao.findByEntity("Order", 99L)).thenReturn(List.of(mockLog));

        List<AuditLog> result = service.getLogs("Order", 99L);

        assertEquals(1, result.size());
        assertEquals("tester", result.get(0).getActor());
        verify(dao).findByEntity("Order", 99L);
    }

    @Test
    void testGetLogsByTypeDelegatesToDao() {
        AuditLog mockLog = mock(AuditLog.class);
        when(dao.findByType("Cart", 1, 10)).thenReturn(List.of(mockLog));

        List<AuditLog> result = service.getLogsByType("Cart", 1, 10);

        assertEquals(1, result.size());
        verify(dao).findByType("Cart", 1, 10);
    }
}