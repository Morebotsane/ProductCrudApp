package com.example.resources;

import com.example.dto.PaginatedResponse;
import com.example.dto.ProductRequest;
import com.example.dto.ProductResponse;
import com.example.dto.APIResponse;
import com.example.services.ProductService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.math.BigDecimal;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Products", description = "Operations related to Products")
public class ProductResource {

    @Inject
    private ProductService productService;

    @GET
    @Operation(summary = "List products with pagination and filtering")
    @APIResponseSchema(value = PaginatedResponse.class, responseDescription = "Paginated list of products")
    public Response getProducts(
            @Parameter(description = "Page number (starts at 1)") @QueryParam("page") @DefaultValue("1") int page,
            @Parameter(description = "Page size") @QueryParam("size") @DefaultValue("10") int size,
            @Parameter(description = "Optional name filter") @QueryParam("name") String nameFilter,
            @Parameter(description = "Optional minimum price") @QueryParam("minPrice") BigDecimal minPrice,
            @Parameter(description = "Optional maximum price") @QueryParam("maxPrice") BigDecimal maxPrice,
            @Parameter(description = "Only return in-stock products") @QueryParam("inStock") Boolean inStock
    ) {
        PaginatedResponse<ProductResponse> products =
                productService.getProducts(page, size, nameFilter, minPrice, maxPrice, inStock);

        return Response.ok(new APIResponse<>(true, "Products fetched successfully", products)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get product by ID")
    @APIResponseSchema(value = ProductResponse.class, responseDescription = "Product details")
    public Response getProductById(@Parameter(description = "Product ID") @PathParam("id") Long id) {
        ProductResponse product = productService.getProductById(id);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(new APIResponse<>(false, "Product not found", null))
                           .build();
        }
        return Response.ok(new APIResponse<>(true, "Product retrieved successfully", product)).build();
    }

    @POST
    @Operation(summary = "Create a new product")
    @APIResponseSchema(value = ProductResponse.class, responseDescription = "Created product")
    public Response createProduct(@Valid ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return Response.status(Response.Status.CREATED)
                       .entity(new APIResponse<>(true, "Product created successfully", created))
                       .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update an existing product")
    @APIResponseSchema(value = ProductResponse.class, responseDescription = "Updated product")
    public Response updateProduct(
            @Parameter(description = "Product ID") @PathParam("id") Long id,
            @Valid ProductRequest request
    ) {
        ProductResponse updated = productService.updateProduct(id, request);
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(new APIResponse<>(false, "Product not found", null))
                           .build();
        }
        return Response.ok(new APIResponse<>(true, "Product updated successfully", updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a product by ID")
    @APIResponseSchema(value = Void.class, responseDescription = "Product deleted")
    public Response deleteProduct(@Parameter(description = "Product ID") @PathParam("id") Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(new APIResponse<>(false, "Product not found", null))
                           .build();
        }
        return Response.ok(new APIResponse<>(true, "Product deleted successfully", null)).build();
    }
}