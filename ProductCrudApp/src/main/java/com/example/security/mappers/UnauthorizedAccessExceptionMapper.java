package com.example.security.mappers;

import com.example.security.exceptions.UnauthorizedAccessException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class UnauthorizedAccessExceptionMapper implements ExceptionMapper<UnauthorizedAccessException> {

    @Override
    public Response toResponse(UnauthorizedAccessException exception) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Forbidden");
        body.put("message", exception.getMessage());

        return Response.status(Response.Status.FORBIDDEN)
                .entity(body)
                .build();
    }
}
