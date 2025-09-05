package com.example.resources;

import com.example.dto.OrderResponse;
import com.example.dto.UpdateStatusRequest;
import com.example.entities.OrderStatus;
import com.example.services.OrderService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;

import com.example.audit.Audited;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.container.ContainerRequestContext;

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
    @Audited(action = "LIST_ORDERS")
    public Response getAllOrders(@Context ContainerRequestContext requestContext) {
        List<OrderResponse> orders = orderService.getAllOrderDtos();

        // no specific entityId here, just listing all orders
        requestContext.setProperty("entityType", "Order");
        requestContext.setProperty("auditPayload", "{ \"count\": " + orders.size() + " }");

        return Response.ok(orders).build();
    }

    // -------------------------
    // GET ORDER BY ID
    // -------------------------
    @GET
    @Path("/{orderId}")
    @Audited(action = "VIEW_ORDER")
    public Response getOrderById(@PathParam("orderId") Long orderId,
                                 @Context ContainerRequestContext requestContext) {
        try {
            OrderResponse order = orderService.getOrderDto(orderId);

            // Pass metadata to the audit filter
            requestContext.setProperty("entityType", "Order");
            requestContext.setProperty("entityId", orderId);
            requestContext.setProperty("auditPayload", "{ \"orderId\": " + orderId + " }");

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
    @Audited(action = "CREATE_ORDER")
    public Response createOrderFromCart(@PathParam("cartId") Long cartId,
                                        @Context ContainerRequestContext requestContext) {
        try {
            OrderResponse order = orderService.createOrderFromCartDto(cartId);

            requestContext.setProperty("entityType", "Order");
            requestContext.setProperty("entityId", order.getId());
            requestContext.setProperty("auditPayload",
                    "{ \"cartId\": " + cartId + ", \"orderId\": " + order.getId() + " }");

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
    @Audited(action = "UPDATE_ORDER_STATUS")
    public Response updateOrderStatus(@PathParam("orderId") Long orderId,
                                      UpdateStatusRequest request,
                                      @Context ContainerRequestContext requestContext) {
        try {
            OrderStatus newStatus;
            try {
                newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("Invalid order status: " + request.getStatus())
                               .build();
            }

            OrderResponse updatedOrder = orderService.updateStatusDto(orderId, newStatus);

            requestContext.setProperty("entityType", "Order");
            requestContext.setProperty("entityId", orderId);
            requestContext.setProperty("auditPayload",
                    "{ \"newStatus\": \"" + newStatus + "\" }");

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
    @Audited(action = "LIST_ORDERS_BY_CUSTOMER")
    public Response getOrdersByCustomer(@PathParam("customerId") Long customerId,
                                        @Context ContainerRequestContext requestContext) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByCustomerDto(customerId);

            requestContext.setProperty("entityType", "Order");
            requestContext.setProperty("auditPayload",
                    "{ \"customerId\": " + customerId + ", \"count\": " + orders.size() + " }");

            return Response.ok(orders).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage())
                           .build();
        }
    }
}
