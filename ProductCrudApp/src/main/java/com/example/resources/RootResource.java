package com.example.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/")
public class RootResource {
    @GET
    public Response root(@Context UriInfo uri) {
        String ui = uri.getBaseUriBuilder().path("swagger-ui").build().toString();
        return Response.seeOther(URI.create(ui)).build();
    }
}
