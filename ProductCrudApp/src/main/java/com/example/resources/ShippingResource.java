package com.example.resources;

import com.example.dto.APIResponse;
import com.example.dto.OrderResponse;
import com.example.dto.ShippingRequest;
import com.example.services.ShippingService;
import com.example.services.AuditService;
import com.example.audit.Audited;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.container.ContainerRequestContext;

@Path("/shipping")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShippingResource {

    @Inject
    private ShippingService shippingService;

    @Inject
    private AuditService auditService;

    private static final String ENTITY_TYPE = "Order";

    // -------------------------
    // SHIP ORDER
    // -------------------------
    @POST
    @Path("/orders/{orderId}/ship")
    @Audited(action = "SHIP_ORDER")
    public Response shipOrder(@PathParam("orderId") Long orderId,
                              ShippingRequest request,
                              @Context ContainerRequestContext ctx) {
        try {
            OrderResponse response = shippingService.shipOrder(orderId, request.getCarrier());

            String payload = String.format("{ \"carrier\": \"%s\" }", request.getCarrier());
            auditService.setAudit(ctx, ENTITY_TYPE, "SHIP_ORDER", orderId, payload);

            return Response.ok(new APIResponse<>(true, "Order shipped successfully", response)).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new APIResponse<>(false, e.getMessage(), null))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new APIResponse<>(false, "Unexpected error: " + e.getMessage(), null))
                    .build();
        }
    }

    // -------------------------
    // DELIVER ORDER
    // -------------------------
    @POST
    @Path("/orders/{orderId}/deliver")
    @Audited(action = "DELIVER_ORDER")
    public Response deliverOrder(@PathParam("orderId") Long orderId,
                                 @Context ContainerRequestContext ctx) {
        try {
            OrderResponse response = shippingService.deliverOrder(orderId);

            auditService.setAudit(ctx, ENTITY_TYPE, "DELIVER_ORDER", orderId, "{}");

            return Response.ok(new APIResponse<>(true, "Order delivered successfully", response)).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new APIResponse<>(false, e.getMessage(), null))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new APIResponse<>(false, "Unexpected error: " + e.getMessage(), null))
                    .build();
        }
    }
}

