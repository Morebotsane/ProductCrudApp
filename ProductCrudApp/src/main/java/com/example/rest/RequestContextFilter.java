package com.example.rest;

import com.example.logging.LogKeys;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class RequestContextFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestContextFilter.class);

    private static final String HDR_REQUEST_ID = "X-Request-Id";
    private static final String HDR_CUSTOMER_ID = "X-Customer-Id";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String reqId = headerOrGenerate(requestContext, HDR_REQUEST_ID);
        String customerId = headerOrDefault(requestContext, HDR_CUSTOMER_ID, "anonymous");

        MDC.put(LogKeys.REQUEST_ID, reqId);
        MDC.put(LogKeys.CUSTOMER_ID, customerId);

        requestContext.setProperty(LogKeys.REQUEST_ID, reqId);
        requestContext.setProperty(LogKeys.CUSTOMER_ID, customerId);

        log.info("REQUEST_START {} {}", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Object reqId = requestContext.getProperty(LogKeys.REQUEST_ID);
        if (reqId != null) responseContext.getHeaders().putSingle(HDR_REQUEST_ID, reqId.toString());

        log.info("REQUEST_END status={} path={}", responseContext.getStatus(), requestContext.getUriInfo().getPath());

        MDC.remove(LogKeys.REQUEST_ID);
        MDC.remove(LogKeys.CUSTOMER_ID);
    }

    private static String headerOrGenerate(ContainerRequestContext ctx, String name) {
        String h = ctx.getHeaderString(name);
        return (h == null || h.isBlank()) ? UUID.randomUUID().toString() : h.trim();
    }

    private static String headerOrDefault(ContainerRequestContext ctx, String name, String def) {
        String h = ctx.getHeaderString(name);
        return (h == null || h.isBlank()) ? def : h.trim();
    }
}