package com.example.resources;

import com.example.dto.CartItemRequest;
import com.example.dto.CartResponse;
import com.example.dto.mappers.CartMapper;
import com.example.services.CartService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/carts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {

    @Inject
    private CartService cartService;

    @Inject
    private CartMapper cartMapper;

    @POST
    @Path("/customer/{customerId}")
    public Response createCartForCustomer(@PathParam("customerId") Long customerId) {
        try {
            CartResponse response = cartMapper.toCartResponse(
                    cartService.createCartForCustomer(customerId)
            );
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/{cartId}/items")
    public Response addProduct(@PathParam("cartId") Long cartId,
                                @Valid CartItemRequest itemRequest) {
        try {
            CartResponse response = cartMapper.toCartResponse(
                    cartService.addProduct(cartId, itemRequest.getProductId(), itemRequest.getQuantity())
            );
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{cartId}/items/{productId}")
    public Response removeProduct(@PathParam("cartId") Long cartId,
                                   @PathParam("productId") Long productId) {
        try {
            CartResponse response = cartMapper.toCartResponse(
                    cartService.removeProduct(cartId, productId)
            );
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/{cartId}/items/{productId}/decrement")
    public Response decrementQuantity(@PathParam("cartId") Long cartId,
                                      @PathParam("productId") Long productId) {
        try {
            CartResponse response = cartMapper.toCartResponse(
                    cartService.decrementProductQuantity(cartId, productId)
            );
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/{cartId}/total")
    public Response getTotal(@PathParam("cartId") Long cartId) {
        try {
            return Response.ok(cartService.getTotal(cartId)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/{cartId}/total-vat")
    public Response getTotalWithVAT(@PathParam("cartId") Long cartId) {
        try {
            return Response.ok(cartService.getTotalWithVAT(cartId)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/{cartId}")
    public Response getCart(@PathParam("cartId") Long cartId) {
        try {
            CartResponse response = cartMapper.toCartResponse(
                    cartService.getCartById(cartId)
            );
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage()).build();
        }
    }
}
