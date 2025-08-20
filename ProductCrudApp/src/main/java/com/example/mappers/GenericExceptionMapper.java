package com.example.mappers;

import com.example.dto.APIResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        exception.printStackTrace(); // Logs the error on server for debugging
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                					   .entity(new APIResponse<>(false, "Internal server error", null))
                					   .build();
    }
}

