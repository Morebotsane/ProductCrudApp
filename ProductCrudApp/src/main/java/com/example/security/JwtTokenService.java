package com.example.security;

import com.nimbusds.jwt.SignedJWT;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import java.text.ParseException;
import java.util.List;

@RequestScoped
public class JwtTokenService {

    @Inject
    private HttpServletRequest request;
    
    //---------------------------
    //USER INFO
    //---------------------------
    public Long getCurrentUserId() {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new SecurityException("Missing Authorization header");
            }

            String token = authHeader.substring(7);
            SignedJWT jwt = SignedJWT.parse(token);

            // userId (preferred)
            Object userIdClaim = jwt.getJWTClaimsSet().getClaim("userId");
            if (userIdClaim != null) {
                return Long.valueOf(userIdClaim.toString());
            }

            // sub (fallback)
            String sub = jwt.getJWTClaimsSet().getSubject();
            if (sub != null && !sub.isBlank()) {
                return Long.valueOf(sub);
            }

            throw new SecurityException("JWT missing both userId and sub");

        } catch (ParseException | NumberFormatException e) {
            throw new SecurityException("Invalid JWT: " + e.getMessage(), e);
        }
    }

    public String getUsername() {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return "unknown";
            }

            String token = authHeader.substring(7);
            SignedJWT jwt = SignedJWT.parse(token);

            // upn (preferred)
            Object upn = jwt.getJWTClaimsSet().getClaim("upn");
            if (upn != null) return upn.toString();

            // fallback to sub
            return jwt.getJWTClaimsSet().getSubject();

        } catch (Exception e) {
            return "unknown";
        }
    }
    
    //-------------------------------
    //ROLE HELPERS
    //-------------------------------
    @SuppressWarnings("unchecked")
    private List<String> getRoles() {
        try {
            SignedJWT jwt = parseToken();
            Object claim = jwt.getJWTClaimsSet().getClaim("groups");
            if (claim instanceof List) {
                return (List<String>) claim;
            }
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    public boolean isCustomer() {
        return hasRole("ROLE_CUSTOMER");
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
    
    // --------------------------
    // INTERNAL: Parse JWT
    // --------------------------
    private SignedJWT parseToken() throws ParseException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        return SignedJWT.parse(token);
    }
}
