package com.example.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/ping")
@Tag(name = "Ping", description = "Testing resource")
public class PingResource {

    @GET
    @Operation(summary = "Simple ping", description = "Returns a static pong")
    public String ping() {
        return "pong";
    }
}
