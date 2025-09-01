package com.example.resources;

import com.example.dto.CartItemRequest;
import com.example.dto.CartResponse;
import com.example.dto.mappers.CartMapper;
import com.example.services.CartService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.math.BigDecimal;

@Path("/carts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {

    @Inject
    private CartService cartService;

    @Inject
    private CartMapper cartMapper;

    // -------------------------
    // Helper for standardized responses
    // -------------------------
    private Response wrapCartResponse(CartResponse cartResponse, Response.Status status) {
        return Response.status(status).entity(cartResponse).build();
    }

    private Response wrapError(String message, Response.Status status) {
        return Response.status(status).entity(message).build();
    }

    // -------------------------
    // Cart endpoints
    // -------------------------

    @POST
    @Path("/customer/{customerId}")
    public Response createCart(@PathParam("customerId") Long customerId) {
        try {
            CartResponse response = cartMapper.toCartResponse(
                    cartService.getOrCreateActiveCart(customerId)
            );
            return wrapCartResponse(response, Response.Status.CREATED);
        } catch (IllegalArgumentException e) {
            return wrapError(e.getMessage(), Response.Status.NOT_FOUND);
        } catch (Exception e) {
            return wrapError("Unexpected error: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/{cartId}/items")
    public Response addProduct(@PathParam("cartId") Long cartId, @Valid CartItemRequest itemRequest) {
        try {
            CartResponse response = cartMapper.toCartResponse(
                    cartService.addProduct(cartId, itemRequest.getProductId(), itemRequest.getQuantity())
            );
            return wrapCartResponse(response, Response.Status.OK);
        } catch (IllegalArgumentException e) {
            return wrapError(e.getMessage(), Response.Status.NOT_FOUND);
        } catch (Exception e) {
            return wrapError("Unexpected error: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/{cartId}/items/{productId}")
    public Response removeProduct(@PathParam("cartId") Long cartId, @PathParam("productId") Long productId) {
        try {
            CartResponse response = cartMapper.toCartResponse(
                    cartService.removeProduct(cartId, productId)
            );
            return wrapCartResponse(response, Response.Status.OK);
        } catch (IllegalArgumentException e) {
            return wrapError(e.getMessage(), Response.Status.NOT_FOUND);
        } catch (Exception e) {
            return wrapError("Unexpected error: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/{cartId}/items/{productId}/decrement")
    public Response decrementProduct(@PathParam("cartId") Long cartId, @PathParam("productId") Long productId) {
        try {
            CartResponse response = cartMapper.toCartResponse(
                    cartService.decrementProductQuantity(cartId, productId)
            );
            return wrapCartResponse(response, Response.Status.OK);
        } catch (IllegalArgumentException e) {
            return wrapError(e.getMessage(), Response.Status.NOT_FOUND);
        } catch (Exception e) {
            return wrapError("Unexpected error: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/{cartId}/clear")
    public Response clearCart(@PathParam("cartId") Long cartId) {
        try {
            CartResponse response = cartMapper.toCartResponse(
                    cartService.clearCart(cartId)
            );
            return wrapCartResponse(response, Response.Status.OK);
        } catch (IllegalArgumentException e) {
            return wrapError(e.getMessage(), Response.Status.NOT_FOUND);
        } catch (Exception e) {
            return wrapError("Unexpected error: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------
    // Totals endpoints
    // -------------------------

    @GET
    @Path("/{cartId}/total")
    public Response getTotal(@PathParam("cartId") Long cartId) {
        try {
            BigDecimal total = cartService.getTotal(cartService.getCartById(cartId));
            return Response.ok(total).build();
        } catch (IllegalArgumentException e) {
            return wrapError(e.getMessage(), Response.Status.NOT_FOUND);
        } catch (Exception e) {
            return wrapError("Unexpected error: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/{cartId}/total-vat")
    public Response getTotalWithVAT(@PathParam("cartId") Long cartId) {
        try {
            BigDecimal totalVAT = cartService.getTotalWithVAT(cartService.getCartById(cartId));
            return Response.ok(totalVAT).build();
        } catch (IllegalArgumentException e) {
            return wrapError(e.getMessage(), Response.Status.NOT_FOUND);
        } catch (Exception e) {
            return wrapError("Unexpected error: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/{cartId}")
    public Response getCart(@PathParam("cartId") Long cartId) {
        try {
            CartResponse response = cartMapper.toCartResponse(
                    cartService.getCartById(cartId)
            );
            return wrapCartResponse(response, Response.Status.OK);
        } catch (IllegalArgumentException e) {
            return wrapError(e.getMessage(), Response.Status.NOT_FOUND);
        } catch (Exception e) {
            return wrapError("Unexpected error: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
