package com.example.resources;

import com.example.entities.AuditLog;
import com.example.services.AuditService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/audits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditResource {

    @Inject
    private AuditService auditService;

    // ---------------------------------------
    // Get all logs for a specific entity
    // Example: GET /audits/entity/Order/10
    // ---------------------------------------
    @GET
    @Path("/entity/{type}/{id}")
    public Response getLogsForEntity(
            @PathParam("type") String entityType,
            @PathParam("id") Long entityId
    ) {
        List<AuditLog> logs = auditService.getLogs(entityType, entityId);
        return Response.ok(logs).build();
    }

    // ---------------------------------------
    // Get logs by entity type with paging
    // Example: GET /audits/type/Order?page=1&size=10
    // ---------------------------------------
    @GET
    @Path("/type/{type}")
    public Response getLogsByType(
            @PathParam("type") String entityType,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        List<AuditLog> logs = auditService.getLogsByType(entityType, page, size);
        return Response.ok(logs).build();
    }
    
    @POST
    @Path("/test")
    public Response createTestLog() {
        auditService.record("system", "TEST_ACTION", "Order", 1L, "{ \"note\": \"manual test\" }");
        return Response.ok().entity("Audit log created").build();
    }
}
