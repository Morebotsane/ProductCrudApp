package com.example.resources;

import com.example.dto.CustomerRequest;
import com.example.dto.CustomerResponse;
import com.example.dto.PaginatedResponse;
import com.example.dto.APIResponse;
import com.example.services.CustomerService;
import com.example.services.AuditService;
import com.example.audit.Audited;

//import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.container.ContainerRequestContext;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customers", description = "Operations related to customers")
public class CustomerResource {

    @Inject
    private CustomerService customerService;

    @Inject
    private AuditService auditService;

    private static final String ENTITY_TYPE = "Customer";

    // -------------------------
    // CREATE CUSTOMER
    // -------------------------
    @POST
    @Audited(action = "CREATE_CUSTOMER")
    @RolesAllowed("ROLE_CUSTOMER")
    public Response createCustomer(@Valid CustomerRequest request,
                                   @Context ContainerRequestContext ctx) {
        CustomerResponse response = customerService.createCustomer(request);

        String payload = String.format(
                "{ \"email\": \"%s\", \"firstName\": \"%s\", \"lastName\": \"%s\" }",
                request.getEmail(), request.getFirstName(), request.getLastName()
        );

        auditService.setAudit(ctx, ENTITY_TYPE, "CREATE_CUSTOMER", response.getId(), payload);

        return Response.status(Response.Status.CREATED)
                .entity(new APIResponse<>(true, "Customer created successfully", response))
                .build();
    }

    // -------------------------
    // GET CUSTOMER BY ID
    // -------------------------
    @GET
    @Path("/{id}")
    @Audited(action = "VIEW_CUSTOMER")
    @RolesAllowed({"ROLE_CUSTOMER","ROLE_ADMIN"})
    public Response getCustomer(@PathParam("id") Long id,
                                @Context ContainerRequestContext ctx) {
        CustomerResponse customer = customerService.getCustomerById(id);

        auditService.setAudit(ctx, ENTITY_TYPE, "VIEW_CUSTOMER", id, "{}");

        if (customer == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Customer not found", null))
                    .build();
        }
        return Response.ok(new APIResponse<>(true, "Customer retrieved successfully", customer)).build();
    }

    // -------------------------
    // LIST CUSTOMERS
    // -------------------------
    @GET
    @Audited(action = "LIST_CUSTOMERS")
    @RolesAllowed("ROLE_ADMIN")
    public Response getCustomers(@QueryParam("page") @DefaultValue("1") int page,
                                 @QueryParam("size") @DefaultValue("10") int size,
                                 @QueryParam("name") String emailFilter,
                                 @Context ContainerRequestContext ctx) {
        PaginatedResponse<CustomerResponse> customers = customerService.getCustomers(page, size, emailFilter);

        String payload = String.format(
                "{ \"page\": %d, \"size\": %d, \"filter\": \"%s\" }",
                page, size, emailFilter
        );

        auditService.setAudit(ctx, ENTITY_TYPE, "LIST_CUSTOMERS", null, payload);

        return Response.ok(new APIResponse<>(true, "Customers fetched successfully", customers)).build();
    }

    // -------------------------
    // UPDATE CUSTOMER
    // -------------------------
    @PUT
    @Path("/{id}")
    @Audited(action = "UPDATE_CUSTOMER")
    @RolesAllowed({"ROLE_CUSTOMER","ROLE_ADMIN"})
    public Response updateCustomer(@PathParam("id") Long id,
                                   @Valid CustomerRequest request,
                                   @Context ContainerRequestContext ctx) {
        CustomerResponse updated = customerService.updateCustomer(id, request);

        String payload = String.format(
                "{ \"email\": \"%s\", \"firstName\": \"%s\", \"lastName\": \"%s\" }",
                request.getEmail(), request.getFirstName(), request.getLastName()
        );

        auditService.setAudit(ctx, ENTITY_TYPE, "UPDATE_CUSTOMER", id, payload);

        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Customer not found", null))
                    .build();
        }
        return Response.ok(new APIResponse<>(true, "Customer updated successfully", updated)).build();
    }

    // -------------------------
    // DELETE CUSTOMER
    // -------------------------
    @DELETE
    @Path("/{id}")
    @Audited(action = "DELETE_CUSTOMER")
    @RolesAllowed({"ROLE_CUSTOMER","ROLE_ADMIN"})
    public Response deleteCustomer(@PathParam("id") Long id,
                                   @Context ContainerRequestContext ctx) {
        boolean deleted = customerService.deleteCustomer(id);

        auditService.setAudit(ctx, ENTITY_TYPE, "DELETE_CUSTOMER", id, "{}");

        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Customer not found", null))
                    .build();
        }
        return Response.ok(new APIResponse<>(true, "Customer deleted successfully", null)).build();
    }
}
