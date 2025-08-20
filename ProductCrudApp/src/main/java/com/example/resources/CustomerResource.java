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

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    private CustomerService customerService;

    /** Create a new customer */
    @POST
    public Response createCustomer(@Valid CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /** Get customer by ID */
    @GET
    @Path("/{id}")
    public Response getCustomer(@PathParam("id") Long id) {
        CustomerResponse response = customerService.getCustomerById(id);
        if (response == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Customer not found")
                           .build();
        }
        return Response.ok(response).build();
    }

    /** List customers with pagination and optional email filter */
    @GET
    public Response getCustomers(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("email") String emailFilter
    ) {
        PaginatedResponse<CustomerResponse> response = customerService.getCustomers(page, size, emailFilter);
        return Response.ok(response).build();
    }

    /** Update an existing customer */
    @PUT
    @Path("/{id}")
    public Response updateCustomer(
            @PathParam("id") Long id,
            @Valid CustomerRequest request
    ) {
        CustomerResponse updated = customerService.updateCustomer(id, request);
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Customer not found")
                           .build();
        }
        return Response.ok(updated).build();
    }

    /** Delete customer by ID */
    @DELETE
    @Path("/{id}")
    public Response deleteCustomer(@PathParam("id") Long id) {
        boolean deleted = customerService.deleteCustomer(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Customer not found")
                           .build();
        }
        return Response.ok().entity("Customer deleted successfully").build();
    }
}


