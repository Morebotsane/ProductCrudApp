package com.example.resources;

import com.example.dto.OrderResponse;
import com.example.dto.UpdateStatusRequest;
import com.example.entities.OrderStatus;
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

    // -------------------------
    // GET ALL ORDERS
    // -------------------------
    @GET
    public Response getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrderDtos();
        return Response.ok(orders).build();
    }

    // -------------------------
    // GET ORDER BY ID
    // -------------------------
    @GET
    @Path("/{orderId}")
    public Response getOrderById(@PathParam("orderId") Long orderId) {
        try {
            OrderResponse order = orderService.getOrderDto(orderId);
            return Response.ok(order).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(e.getMessage())
                           .build();
        }
    }

    // -------------------------
    // CREATE ORDER FROM CART
    // -------------------------
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
                           .entity("Unexpected error: " + e.getMessage())
                           .build();
        }
    }

    // -------------------------
    // UPDATE ORDER STATUS
    // -------------------------
    @PUT
    @Path("/{orderId}/status")
    public Response updateOrderStatus(@PathParam("orderId") Long orderId,
                                      UpdateStatusRequest request) {
        try {
            // Safely convert string → enum
            OrderStatus newStatus;
            try {
                newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("Invalid order status: " + request.getStatus())
                               .build();
            }

            OrderResponse updatedOrder = orderService.updateStatusDto(orderId, newStatus);
            return Response.ok(updatedOrder).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage())
                           .build();
        }
    }

    // -------------------------
    // GET ORDERS BY CUSTOMER
    // -------------------------
    @GET
    @Path("/customer/{customerId}")
    public Response getOrdersByCustomer(@PathParam("customerId") Long customerId) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByCustomerDto(customerId);
            return Response.ok(orders).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage())
                           .build();
        }
    }
}
