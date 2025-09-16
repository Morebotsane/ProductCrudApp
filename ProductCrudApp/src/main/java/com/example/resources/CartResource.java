package com.example.resources;

import com.example.audit.Audited;
import com.example.dto.CartItemRequest;
import com.example.dto.CartResponse;
import com.example.dto.APIResponse;
import com.example.dto.mappers.CartMapper;
import com.example.entities.Cart;
import com.example.services.AuditService;
import com.example.services.CartService;

import jakarta.annotation.security.RolesAllowed;
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
    @RolesAllowed("ROLE_CUSTOMER")
    public Response createCart(@PathParam("customerId") Long customerId,
                               @Context ContainerRequestContext ctx) {

        Cart cart = cartService.getOrCreateActiveCart(customerId);
        CartResponse response = cartMapper.toCartResponse(cart);

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
    @RolesAllowed("ROLE_CUSTOMER")
    public Response addProduct(@PathParam("cartId") Long cartId,
                               @Valid CartItemRequest itemRequest,
                               @Context ContainerRequestContext ctx) {

        Cart cart = cartService.addProduct(cartId, itemRequest.getProductId(), itemRequest.getQuantity());
        CartResponse response = cartMapper.toCartResponse(cart);

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
    @RolesAllowed({"ROLE_CUSTOMER","ROLE_ADMIN"})
    public Response removeProduct(@PathParam("cartId") Long cartId,
                                  @PathParam("productId") Long productId,
                                  @Context ContainerRequestContext ctx) {

        Cart cart = cartService.removeProduct(cartId, productId);
        CartResponse response = cartMapper.toCartResponse(cart);

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
    @RolesAllowed({"ROLE_CUSTOMER","ROLE_ADMIN"})
    public Response decrementProduct(@PathParam("cartId") Long cartId,
                                     @PathParam("productId") Long productId,
                                     @Context ContainerRequestContext ctx) {

        Cart cart = cartService.decrementProductQuantity(cartId, productId);
        CartResponse response = cartMapper.toCartResponse(cart);

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
    @RolesAllowed({"ROLE_CUSTOMER","ROLE_ADMIN"})
    public Response clearCart(@PathParam("cartId") Long cartId,
                              @Context ContainerRequestContext ctx) {

        Cart cart = cartService.clearCart(cartId);
        CartResponse response = cartMapper.toCartResponse(cart);

        auditService.setAudit(ctx, ENTITY_TYPE, "CLEAR_CART", cartId, "{}");

        return Response.ok(new APIResponse<>(true, "Cart cleared successfully", response)).build();
    }

    // -------------------------
    // VIEW CART
    // -------------------------
    @GET
    @Path("/{cartId}")
    @Audited(action = "VIEW_CART")
    @RolesAllowed({"ROLE_CUSTOMER","ROLE_ADMIN"})
    public Response getCart(@PathParam("cartId") Long cartId,
                            @Context ContainerRequestContext ctx) {

        Cart cart = cartService.getCartById(cartId);
        CartResponse response = cartMapper.toCartResponse(cart);

        auditService.setAudit(ctx, ENTITY_TYPE, "VIEW_CART", cartId, "{}");

        return Response.ok(new APIResponse<>(true, "Cart retrieved successfully", response)).build();
    }
}

