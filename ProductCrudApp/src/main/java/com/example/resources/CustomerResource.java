package com.example.resources;

import com.example.dto.CustomerRequest;
import com.example.dto.CustomerResponse;
import com.example.dto.PaginatedResponse;
import com.example.dto.APIResponse;
import com.example.services.CustomerService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customers", description = "Operations related to customers")
public class CustomerResource {

    @Inject
    private CustomerService customerService;

    @POST
    @Operation(summary = "Create a new customer")
    @APIResponseSchema(value = CustomerResponse.class, responseDescription = "Created customer")
    public Response createCustomer(@Valid CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return Response.status(Response.Status.CREATED)
                .entity(new APIResponse<>(true, "Customer created successfully", response))
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get customer by id")
    @APIResponseSchema(value = CustomerResponse.class, responseDescription = "Customer details")
    public Response getCustomer(@Parameter(description = "Customer ID") @PathParam("id") Long id) {
        CustomerResponse customer = customerService.getCustomerById(id);
        if (customer == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Customer not found", null))
                    .build();
        }
        return Response.ok(new APIResponse<>(true, "Customer retrieved successfully", customer)).build();
    }

    @GET
    @Operation(summary = "List of Customers with pagination")
    @APIResponseSchema(value = PaginatedResponse.class, responseDescription = "Paginated list of customers")
    public Response getCustomers(
            @Parameter(description = "Page number (starts at 1)") @QueryParam("page") @DefaultValue("1") int page,
            @Parameter(description = "Page size") @QueryParam("size") @DefaultValue("10") int size,
            @Parameter(description = "Optional name filter") @QueryParam("name") String emailFilter
    ) {
        PaginatedResponse<CustomerResponse> customers = customerService.getCustomers(page, size, emailFilter);
        return Response.ok(new APIResponse<>(true, "Customers fetched successfully", customers)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update an existing customer by id")
    @APIResponseSchema(value = CustomerResponse.class, responseDescription = "Customer updated")
    public Response updateCustomer(@PathParam("id") Long id, @Valid CustomerRequest request) {
        CustomerResponse updated = customerService.updateCustomer(id, request);
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Customer not found", null))
                    .build();
        }
        return Response.ok(new APIResponse<>(true, "Customer updated successfully", updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete customer by id")
    @APIResponseSchema(value = CustomerResponse.class, responseDescription = "Customer deleted")
    public Response deleteCustomer(@PathParam("id") Long id) {
        boolean deleted = customerService.deleteCustomer(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Customer not found", null))
                    .build();
        }
        return Response.ok(new APIResponse<>(true, "Customer deleted successfully", null)).build();
    }
}
