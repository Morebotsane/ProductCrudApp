package com.example.resources;

import com.example.audit.Audited;
import com.example.dto.CartItemRequest;
import com.example.dto.CartResponse;
import com.example.dto.APIResponse;
import com.example.dto.mappers.CartMapper;
import com.example.services.AuditService;
import com.example.services.CartService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.container.ContainerRequestContext;

@Path("/carts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {

    @Inject
    private CartService cartService;

    @Inject
    private CartMapper cartMapper;

    @Inject
    private AuditService auditService;

    private static final String ENTITY_TYPE = "Cart";

    // -------------------------
    // CREATE CART
    // -------------------------
    @POST
    @Path("/customer/{customerId}")
    @Audited(action = "CREATE_CART")
    public Response createCart(@PathParam("customerId") Long customerId,
                               @Context ContainerRequestContext ctx) {

        CartResponse response = cartMapper.toCartResponse(
                cartService.getOrCreateActiveCart(customerId)
        );

        String payload = String.format("{ \"customerId\": %d }", customerId);
        auditService.setAudit(ctx, ENTITY_TYPE, "CREATE_CART", response.getId(), payload);

        return Response.status(Response.Status.CREATED)
                .entity(new APIResponse<>(true, "Cart created successfully", response))
                .build();
    }

    // -------------------------
    // ADD PRODUCT TO CART
    // -------------------------
    @POST
    @Path("/{cartId}/items")
    @Audited(action = "ADD_PRODUCT_TO_CART")
    public Response addProduct(@PathParam("cartId") Long cartId,
                               @Valid CartItemRequest itemRequest,
                               @Context ContainerRequestContext ctx) {

        CartResponse response = cartMapper.toCartResponse(
                cartService.addProduct(cartId, itemRequest.getProductId(), itemRequest.getQuantity())
        );

        String payload = String.format("{ \"productId\": %d, \"quantity\": %d }",
                itemRequest.getProductId(), itemRequest.getQuantity());

        auditService.setAudit(ctx, ENTITY_TYPE, "ADD_PRODUCT_TO_CART", cartId, payload);

        return Response.ok(new APIResponse<>(true, "Product added to cart", response)).build();
    }

    // -------------------------
    // REMOVE PRODUCT FROM CART
    // -------------------------
    @DELETE
    @Path("/{cartId}/items/{productId}")
    @Audited(action = "REMOVE_PRODUCT_FROM_CART")
    public Response removeProduct(@PathParam("cartId") Long cartId,
                                  @PathParam("productId") Long productId,
                                  @Context ContainerRequestContext ctx) {

        CartResponse response = cartMapper.toCartResponse(
                cartService.removeProduct(cartId, productId)
        );

        String payload = String.format("{ \"productId\": %d }", productId);
        auditService.setAudit(ctx, ENTITY_TYPE, "REMOVE_PRODUCT_FROM_CART", cartId, payload);

        return Response.ok(new APIResponse<>(true, "Product removed from cart", response)).build();
    }

    // -------------------------
    // DECREMENT PRODUCT IN CART
    // -------------------------
    @POST
    @Path("/{cartId}/items/{productId}/decrement")
    @Audited(action = "DECREMENT_PRODUCT_IN_CART")
    public Response decrementProduct(@PathParam("cartId") Long cartId,
                                     @PathParam("productId") Long productId,
                                     @Context ContainerRequestContext ctx) {

        CartResponse response = cartMapper.toCartResponse(
                cartService.decrementProductQuantity(cartId, productId)
        );

        String payload = String.format("{ \"productId\": %d }", productId);
        auditService.setAudit(ctx, ENTITY_TYPE, "DECREMENT_PRODUCT_IN_CART", cartId, payload);

        return Response.ok(new APIResponse<>(true, "Product quantity decremented", response)).build();
    }

    // -------------------------
    // CLEAR CART
    // -------------------------
    @POST
    @Path("/{cartId}/clear")
    @Audited(action = "CLEAR_CART")
    public Response clearCart(@PathParam("cartId") Long cartId,
                              @Context ContainerRequestContext ctx) {

        CartResponse response = cartMapper.toCartResponse(
                cartService.clearCart(cartId)
        );

        auditService.setAudit(ctx, ENTITY_TYPE, "CLEAR_CART", cartId, "{}");

        return Response.ok(new APIResponse<>(true, "Cart cleared successfully", response)).build();
    }

    // -------------------------
    // VIEW CART
    // -------------------------
    @GET
    @Path("/{cartId}")
    @Audited(action = "VIEW_CART")
    public Response getCart(@PathParam("cartId") Long cartId,
                            @Context ContainerRequestContext ctx) {

        CartResponse response = cartMapper.toCartResponse(
                cartService.getCartById(cartId)
        );

        auditService.setAudit(ctx, ENTITY_TYPE, "VIEW_CART", cartId, "{}");

        return Response.ok(new APIResponse<>(true, "Cart retrieved successfully", response)).build();
    }
}
