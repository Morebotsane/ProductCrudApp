package com.example.resources;

import com.example.dto.*;
import com.example.services.ProductService;
import com.example.services.AuditService;
import com.example.audit.Audited;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.container.ContainerRequestContext;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;

import jakarta.annotation.security.RolesAllowed;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Products", description = "Operations related to Products")
public class ProductResource {

    @Inject
    private ProductService productService;

    @Inject
    private AuditService auditService;

    private static final String ENTITY_TYPE = "Product";

    @GET
    @Audited(action = "LIST_PRODUCTS")
    @RolesAllowed({"ROLE_CUSTOMER", "ROLE_ADMIN", "ROLE_SUPER"})  // all authenticated users
    public Response getProducts(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("name") String nameFilter,
            @QueryParam("minPrice") BigDecimal minPrice,
            @QueryParam("maxPrice") BigDecimal maxPrice,
            @QueryParam("inStock") Boolean inStock,
            @Context ContainerRequestContext requestContext
    ) {
        PaginatedResponse<ProductResponse> products =
                productService.getProducts(page, size, nameFilter, minPrice, maxPrice, inStock);

        String payload = String.format(
                "{\"page\":%d,\"size\":%d,\"filter\":\"%s\",\"minPrice\":%s,\"maxPrice\":%s,\"inStock\":%s}",
                page, size, nameFilter, minPrice, maxPrice, inStock
        );
        auditService.setAudit(requestContext, ENTITY_TYPE, "LIST_PRODUCTS", null, payload);

        return Response.ok(new APIResponse<>(true, "Products fetched successfully", products)).build();
    }

    @GET
    @Path("/{id}")
    @Audited(action = "VIEW_PRODUCT")
    @RolesAllowed({"ROLE_CUSTOMER", "ROLE_ADMIN", "ROLE_SUPER"})  // all authenticated users
    public Response getProductById(
            @PathParam("id") Long id,
            @Context ContainerRequestContext requestContext
    ) {
        ProductResponse product = productService.getProductById(id);
        auditService.setAudit(requestContext, ENTITY_TYPE, "VIEW_PRODUCT", id, "{}");

        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Product not found", null))
                    .build();
        }
        return Response.ok(new APIResponse<>(true, "Product retrieved successfully", product)).build();
    }

    @POST
    @Audited(action = "CREATE_PRODUCT")
    @RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER"})  // restricted
    public Response createProduct(
            @Valid ProductRequest request,
            @Context ContainerRequestContext requestContext
    ) {
        ProductResponse created = productService.createProduct(request);
        String payload = String.format("{\"name\":\"%s\",\"price\":%s}", request.getName(), request.getPrice());
        auditService.setAudit(requestContext, ENTITY_TYPE, "CREATE_PRODUCT", created.getId(), payload);

        return Response.status(Response.Status.CREATED)
                .entity(new APIResponse<>(true, "Product created successfully", created))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Audited(action = "UPDATE_PRODUCT")
    @RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER"})  // restricted
    public Response updateProduct(
            @PathParam("id") Long id,
            @Valid ProductRequest request,
            @Context ContainerRequestContext requestContext
    ) {
        ProductResponse updated = productService.updateProduct(id, request);
        String payload = String.format("{\"name\":\"%s\",\"price\":%s}", request.getName(), request.getPrice());
        auditService.setAudit(requestContext, ENTITY_TYPE, "UPDATE_PRODUCT", id, payload);

        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Product not found", null))
                    .build();
        }
        return Response.ok(new APIResponse<>(true, "Product updated successfully", updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Audited(action = "DELETE_PRODUCT")
    @RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER"})  // restricted
    public Response deleteProduct(
            @PathParam("id") Long id,
            @Context ContainerRequestContext requestContext
    ) {
        boolean deleted = productService.deleteProduct(id);
        auditService.setAudit(requestContext, ENTITY_TYPE, "DELETE_PRODUCT", id, "{}");

        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Product not found", null))
                    .build();
        }
        return Response.ok(new APIResponse<>(true, "Product deleted successfully", null)).build();
    }
}