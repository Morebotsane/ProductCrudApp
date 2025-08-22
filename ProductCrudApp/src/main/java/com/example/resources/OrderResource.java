package com.example.resources;

import com.example.dto.OrderResponse;
import com.example.dto.UpdateStatusRequest;
import com.example.services.OrderService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    private OrderService orderService;

    // Get all orders as DTOs
    @GET
    public Response getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrderDtos();
        return Response.ok(orders).build();
    }

    // Get a single order by ID as a DTO
    @GET
    @Path("/{orderId}")
    public Response getOrderById(@PathParam("orderId") Long orderId) {
        try {
            OrderResponse order = orderService.getOrderDto(orderId);
            return Response.ok(order).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    // Create order from cart, returning DTO
    @POST
    @Path("/from-cart/{cartId}")
    public Response createOrderFromCart(@PathParam("cartId") Long cartId) {
        try {
            OrderResponse order = orderService.createOrderFromCartDto(cartId);
            return Response.status(Response.Status.CREATED).entity(order).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage()).build();
        }
    }

    // Update order status, returning DTO
    @PUT
    @Path("/{orderId}/status")
    public Response updateOrderStatus(@PathParam("orderId") Long orderId, UpdateStatusRequest request) {
        try {
            OrderResponse updatedOrder = orderService.updateStatusDto(orderId, request.getStatus());
            return Response.ok(updatedOrder).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage()).build();
        }
    }

    // Get orders for a customer as DTOs
    @GET
    @Path("/customer/{customerId}")
    public Response getOrdersByCustomer(@PathParam("customerId") Long customerId) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByCustomerDto(customerId);
            return Response.ok(orders).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage()).build();
        }
    }
}



