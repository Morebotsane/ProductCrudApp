package com.example.security;

import jakarta.annotation.Priority;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.ws.rs.core.HttpHeaders;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;

import java.security.interfaces.RSAPublicKey;
import java.util.*;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;

@Alternative
@ApplicationScoped
@Priority(APPLICATION + 10) // makes this bean the preferred one
public class JwtAuthMechanism implements HttpAuthenticationMechanism {

    private RSAPublicKey publicKey;

    @PostConstruct
    private void init() {
        try {
            this.publicKey = KeyLoader.loadPublicKey("META-INF/jwt-public.pem");
        } catch (Exception e) {
            throw new IllegalStateException("Could not load public key for JWT verification", e);
        }
    }

    @Override
    public jakarta.security.enterprise.AuthenticationStatus validateRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpMessageContext context) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                SignedJWT jwt = SignedJWT.parse(token);
                JWSVerifier verifier = new RSASSAVerifier(publicKey);

                if (!jwt.verify(verifier)) {
                    return context.responseUnauthorized();
                }

                var claims = jwt.getJWTClaimsSet();

                Date exp = claims.getExpirationTime();
                if (exp == null || new Date().after(exp)) {
                    return context.responseUnauthorized();
                }

                if (!"http://localhost:8080/issuer".equals(claims.getIssuer())) {
                    return context.responseUnauthorized();
                }

                String username = Optional.ofNullable((String) claims.getClaim("upn"))
                                          .orElse(claims.getSubject());

                Object groupsClaim = claims.getClaim("groups");
                Set<String> groups = new HashSet<>();
                if (groupsClaim instanceof List<?>) {
                    for (Object g : (List<?>) groupsClaim) {
                        String role = String.valueOf(g);
                        groups.add(role);

                        if ("ROLE_SUPER".equals(role)) {
                            groups.add("ROLE_ADMIN");
                            groups.add("ROLE_CUSTOMER");
                        }
                    }
                }

                CredentialValidationResult result =
                        new CredentialValidationResult(username, groups);

                System.out.println("=== JWT Authentication Debug ===");
                System.out.println("Username: " + username);
                System.out.println("Groups: " + groups);
                System.out.println("================================");

                return context.notifyContainerAboutLogin(result);

            } catch (Exception e) {
                return context.responseUnauthorized();
            }
        }

        return context.doNothing();
    }
}