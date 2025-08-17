package com.example.exception;

import com.example.dto.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {
    @Override
    public Response toResponse(IllegalStateException ex) {
        return Response.status(Response.Status.CONFLICT)
                	   .entity(new ErrorResponse(409, "Conflict", ex.getMessage()))
                	   .build();
    }
}

