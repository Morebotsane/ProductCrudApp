package com.example.security.mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class AppExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        int status = mapStatus(exception);

        Map<String, Object> body = new HashMap<>();
        body.put("error", exception.getClass().getSimpleName());
        body.put("message", exception.getMessage() != null ? exception.getMessage() : "Unexpected error occurred");

        return Response.status(status)
                .entity(body)
                .build();
    }

    private int mapStatus(Throwable e) {
        if (e instanceof IllegalArgumentException) return Response.Status.BAD_REQUEST.getStatusCode();
        if (e instanceof SecurityException) return Response.Status.UNAUTHORIZED.getStatusCode();
        if (e instanceof jakarta.ejb.EJBException) return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }
}
