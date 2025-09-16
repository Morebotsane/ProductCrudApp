package com.example.resources;

import com.example.dto.APIResponse;
import com.example.dto.AddressRequest;
import com.example.dto.AddressResponse;
import com.example.services.CustomerAddressService;
import com.example.services.AuditService;
import com.example.audit.Audited;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/customers/{customerId}/addresses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customer Addresses", description = "Operations related to customer addresses")
public class CustomerAddressResource {

    @Inject
    private CustomerAddressService addressService;

    @Inject
    private AuditService auditService;

    private static final String ENTITY_TYPE = "CustomerAddress";

    // -------------------------
    // ADD ADDRESS
    // -------------------------
    @POST
    @Audited(action = "ADD_ADDRESS")
    @RolesAllowed({"ROLE_CUSTOMER","ROLE_ADMIN"})
    public Response addAddress(@PathParam("customerId") Long customerId,
                               @Valid AddressRequest request,
                               @Context ContainerRequestContext ctx) {
        AddressResponse response = addressService.addAddress(customerId, request);

        String payload = String.format("{ \"customerId\": %d, \"addressId\": %d }",
                customerId, response.getId());
        auditService.setAudit(ctx, ENTITY_TYPE, "ADD_ADDRESS", response.getId(), payload);

        return Response.status(Response.Status.CREATED)
                .entity(new APIResponse<>(true, "Address added successfully", response))
                .build();
    }

    // -------------------------
    // LIST ADDRESSES
    // -------------------------
    @GET
    @Audited(action = "LIST_ADDRESSES")
    @RolesAllowed({"ROLE_CUSTOMER","ROLE_ADMIN"})
    public Response getAddresses(@PathParam("customerId") Long customerId,
                                 @Context ContainerRequestContext ctx) {
        List<AddressResponse> addresses = addressService.getAddresses(customerId);

        String payload = String.format("{ \"customerId\": %d, \"count\": %d }",
                customerId, addresses.size());
        auditService.setAudit(ctx, ENTITY_TYPE, "LIST_ADDRESSES", null, payload);

        return Response.ok(new APIResponse<>(true, "Addresses fetched successfully", addresses)).build();
    }

    // -------------------------
    // UPDATE ADDRESS
    // -------------------------
    @PUT
    @Path("/{addressId}")
    @Audited(action = "UPDATE_ADDRESS")
    @RolesAllowed({"ROLE_CUSTOMER","ROLE_ADMIN"})
    public Response updateAddress(@PathParam("customerId") Long customerId,
                                  @PathParam("addressId") Long addressId,
                                  @Valid AddressRequest request,
                                  @Context ContainerRequestContext ctx) {
        AddressResponse updated = addressService.updateAddress(customerId, addressId, request);

        String payload = String.format("{ \"customerId\": %d, \"addressId\": %d }",
                customerId, addressId);
        auditService.setAudit(ctx, ENTITY_TYPE, "UPDATE_ADDRESS", addressId, payload);

        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Address not found", null))
                    .build();
        }

        return Response.ok(new APIResponse<>(true, "Address updated successfully", updated)).build();
    }

    // -------------------------
    // DELETE ADDRESS
    // -------------------------
    @DELETE
    @Path("/{addressId}")
    @Audited(action = "DELETE_ADDRESS")
    @RolesAllowed({"ROLE_CUSTOMER","ROLE_ADMIN"})
    public Response deleteAddress(@PathParam("customerId") Long customerId,
                                  @PathParam("addressId") Long addressId,
                                  @Context ContainerRequestContext ctx) {
        boolean deleted = addressService.deleteAddress(customerId, addressId);

        String payload = String.format("{ \"customerId\": %d, \"addressId\": %d }",
                customerId, addressId);
        auditService.setAudit(ctx, ENTITY_TYPE, "DELETE_ADDRESS", addressId, payload);

        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Address not found", null))
                    .build();
        }

        return Response.ok(new APIResponse<>(true, "Address deleted successfully", null)).build();
    }
}
