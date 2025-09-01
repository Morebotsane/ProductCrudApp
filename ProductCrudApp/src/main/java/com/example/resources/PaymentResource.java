package com.example.resources;

import com.example.dto.OrderResponse;
import com.example.dto.PaymentRequest;
import com.example.services.OrderService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @Inject
    private OrderService orderService;

    // Simulate payment for an order
    @POST
    @Path("/{orderId}/pay")
    public Response payOrder(@PathParam("orderId") Long orderId, PaymentRequest request) {
        try {
            OrderResponse order = orderService.payOrder(
                    orderId,
                    request.getAmount(),
                    request.getMethod(),
                    request.getTxnRef()
            );
            return Response.ok(order).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(e.getMessage())
                           .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage())
                           .build();
        }
    }
}
