package com.example.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

//import java.io.FileReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class JwtIssuer {

    private static PrivateKey loadPrivateKey(String filename) throws Exception {
        String pem = new String(Files.readAllBytes(Paths.get(filename)));
        pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                 .replace("-----END PRIVATE KEY-----", "")
                 .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public static String issueToken(Long userId, String username, List<String> groups, String privateKeyPath) throws Exception {
        PrivateKey privateKey = loadPrivateKey(privateKeyPath);
        JWSSigner signer = new RSASSASigner(privateKey);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(userId))         // "sub"
                .claim("userId", userId)                 // numeric id
                .claim("upn", username)                  // username/email
                .claim("groups", groups)                 // roles
                .issuer("http://localhost:8080/issuer")    // must match in your validation     
                .audience("myapp")
                .expirationTime(new Date(System.currentTimeMillis() + 3600_000)) // 1h expiry
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                claims
        );

        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    public static void main(String[] args) throws Exception {
        String privateKeyPath = "C:/Users/Morebotsane/Desktop/GitHub Classic Token/jwt-private.pem";

        // Customer
        System.out.println("Customer JWT: " + issueToken(
            42L, "alice", List.of("ROLE_CUSTOMER"), privateKeyPath));

        // Admin
        System.out.println("Admin JWT: " + issueToken(
            1L, "bossman", List.of("ROLE_ADMIN"), privateKeyPath));

        // Superuser (both roles)
        System.out.println("SuperUser JWT: " + issueToken(
            99L, "superuser", List.of("ROLE_CUSTOMER", "ROLE_ADMIN"), privateKeyPath));
    }
}
