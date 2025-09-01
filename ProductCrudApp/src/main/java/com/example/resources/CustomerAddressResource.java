package com.example.resources;

import com.example.dto.APIResponse;
import com.example.dto.AddressRequest;
import com.example.dto.AddressResponse;
import com.example.services.CustomerAddressService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/customers/{customerId}/addresses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customer Addresses", description = "Operations related to customer addresses")
public class CustomerAddressResource {

    @Inject
    private CustomerAddressService addressService;

    @POST
    @Operation(summary = "Add new address for customer")
    @APIResponseSchema(value = AddressResponse.class, responseDescription = "Created address")
    public Response addAddress(@PathParam("customerId") Long customerId,
                               @Valid AddressRequest request) {
        AddressResponse response = addressService.addAddress(customerId, request);
        return Response.status(Response.Status.CREATED)
                .entity(new APIResponse<>(true, "Address added successfully", response))
                .build();
    }

    @GET
    @Operation(summary = "List all addresses of a customer")
    @APIResponseSchema(value = AddressResponse.class, responseDescription = "Customer addresses")
    public Response getAddresses(@PathParam("customerId") Long customerId) {
        List<AddressResponse> addresses = addressService.getAddresses(customerId);
        return Response.ok(new APIResponse<>(true, "Addresses fetched successfully", addresses)).build();
    }

    @PUT
    @Path("/{addressId}")
    @Operation(summary = "Update customer address")
    @APIResponseSchema(value = AddressResponse.class, responseDescription = "Updated address")
    public Response updateAddress(@PathParam("customerId") Long customerId,
                                  @PathParam("addressId") Long addressId,
                                  @Valid AddressRequest request) {
        AddressResponse updated = addressService.updateAddress(customerId, addressId, request);
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Address not found", null))
                    .build();
        }
        return Response.ok(new APIResponse<>(true, "Address updated successfully", updated)).build();
    }

    @DELETE
    @Path("/{addressId}")
    @Operation(summary = "Delete customer address")
    public Response deleteAddress(@PathParam("customerId") Long customerId,
                                  @PathParam("addressId") Long addressId) {
        boolean deleted = addressService.deleteAddress(customerId, addressId);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse<>(false, "Address not found", null))
                    .build();
        }
        return Response.ok(new APIResponse<>(true, "Address deleted successfully", null)).build();
    }
}