package com.example.rest;

import com.example.logging.LogKeys;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        log.error("UNHANDLED_EXCEPTION {}", exception.toString(), exception);

        String requestId = MDC.get(LogKeys.REQUEST_ID);
        if (requestId == null) requestId = "unknown";

        Map<String, Object> body = Map.of(
                "success", false,
                "message", "Internal server error",
                "requestId", requestId
        );
        Jsonb jsonb = JsonbBuilder.create();
        String json = jsonb.toJson(body);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .build();
    }
}