package com.example.mappers;

import com.example.dto.APIResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        // Build readable message for all violated constraints
        String message = exception.getConstraintViolations().stream()
                				  .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                				  .collect(Collectors.joining("; "));

        return Response.status(Response.Status.BAD_REQUEST)
                	   .entity(new APIResponse<>(false, message, null))
                	   .build();
    }
}

