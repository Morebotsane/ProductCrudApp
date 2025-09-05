package com.example.resources;

import com.example.dto.APIResponse;
import com.example.dto.OrderResponse;
import com.example.dto.PaymentRequest;
import com.example.services.OrderService;
import com.example.services.AuditService;
import com.example.audit.Audited;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.container.ContainerRequestContext;

@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @Inject
    private OrderService orderService;

    @Inject
    private AuditService auditService;

    private static final String ENTITY_TYPE = "Order";

    // -------------------------
    // PAY ORDER
    // -------------------------
    @POST
    @Path("/{orderId}/pay")
    @Audited(action = "PAY_ORDER")
    public Response payOrder(@PathParam("orderId") Long orderId,
                             PaymentRequest request,
                             @Context ContainerRequestContext ctx) {
        try {
            OrderResponse order = orderService.payOrder(
                    orderId,
                    request.getAmount(),
                    request.getMethod(),
                    request.getTxnRef()
            );

            String payload = String.format(
                    "{ \"amount\": %s, \"method\": \"%s\", \"txnRef\": \"%s\" }",
                    request.getAmount(), request.getMethod(), request.getTxnRef()
            );

            auditService.setAudit(ctx, ENTITY_TYPE, "PAY_ORDER", orderId, payload);

            return Response.ok(new APIResponse<>(true, "Order payment successful", order)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, e.getMessage(), null))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new APIResponse<>(false, "Unexpected error: " + e.getMessage(), null))
                    .build();
        }
    }
}

