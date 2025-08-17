package com.example.resources;

import com.example.entities.Customer;
import com.example.dao.CustomerDAO;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    private CustomerDAO customerDAO;

    @POST
    public Response createCustomer(Customer customer) {
        try {
            customerDAO.save(customer); // JPA handles ID generation
            return Response.status(Response.Status.CREATED).entity(customer).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getCustomer(@PathParam("id") Long id) {
        Customer customer = customerDAO.findById(Customer.class, id);
        if (customer == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(customer).build();
    }
}

