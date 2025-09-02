package com.example.resources;

import com.example.dto.OrderResponse;
import com.example.dto.ShippingRequest;
import com.example.services.ShippingService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/shipping")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShippingResource {

    @Inject
    private ShippingService shippingService;

    // Ship an order
    @POST
    @Path("/orders/{orderId}/ship")
    public Response shipOrder(@PathParam("orderId") Long orderId, ShippingRequest request) {
        try {
            OrderResponse response = shippingService.shipOrder(orderId, request.getCarrier());
            return Response.ok(response).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(e.getMessage())
                           .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage())
                           .build();
        }
    }

    // Deliver an order
    @POST
    @Path("/orders/{orderId}/deliver")
    public Response deliverOrder(@PathParam("orderId") Long orderId) {
        try {
            OrderResponse response = shippingService.deliverOrder(orderId);
            return Response.ok(response).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(e.getMessage())
                           .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage())
                           .build();
        }
    }
}
