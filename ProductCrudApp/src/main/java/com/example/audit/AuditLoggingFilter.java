package com.example.audit;

import com.example.services.AuditService;
import com.example.logging.LogKeys;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.MDC;

import java.io.IOException;

@Provider
@Audited
@Priority(Priorities.USER)
public class AuditLoggingFilter implements ContainerResponseFilter {

    @Inject
    private AuditService auditService;

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        // Only log successful responses (status 2xx)
        if (responseContext.getStatus() >= 200 && responseContext.getStatus() < 300) {
            Object entityId = requestContext.getProperty("entityId"); // optional: set in resource
            String entityType = (String) requestContext.getProperty("entityType");
            String actor = "customer:" + MDC.get(LogKeys.CUSTOMER_ID); // from MDC

            String action = (String) requestContext.getProperty("auditAction");
            if (action == null) {
                action = requestContext.getMethod(); // fallback: GET/POST/PUT/DELETE
            }

            // optional payload: store request JSON or reason
            String payload = (String) requestContext.getProperty("auditPayload");

            if (entityType != null && entityId != null) {
                auditService.record(actor, action, entityType, (Long) entityId, payload);
            }
        }
    }
}
