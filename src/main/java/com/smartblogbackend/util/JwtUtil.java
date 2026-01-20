package com.smartblogbackend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.smartblogbackend.model.User;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // IMPORTANT: This needs to be static for the single instance we create manually
    private static String secretKeyStatic;

    @Value("${jwt.secret}")
    private String secretKey;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isEmpty()) {
            logger.error("JWT secret key is not configured! Set the jwt.secret property in application.properties or as an environment variable.");
            throw new IllegalStateException("JWT secret key is not configured");
        }

        // Copy to the static field so it's accessible from all methods
        secretKeyStatic = secretKey;
        logger.info("JWT Secret successfully initialized with key length: {}", secretKey.length());
    }

    // Generate JWT Token from User object
    public String generateJwtToken(User user) {
        if (secretKeyStatic == null) {
            logger.error("JWT Secret is null in generateJwtToken!");
            throw new IllegalStateException("JWT Secret is null. Make sure the bean is properly initialized.");
        }

        Algorithm algorithm = Algorithm.HMAC256(secretKeyStatic);
        return JWT.create()
                .withSubject(user.getEmail()) // Use user's email as the subject
                .withClaim("name", user.getName())
                .withClaim("email", user.getEmail())
                .withClaim("userId", user.getId())
                .withClaim("profileImage",user.getProfileImage())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)) // 1 hour expiration
                .sign(algorithm);
    }

    public Long extractUserId(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        // Handle "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secretKeyStatic))
                .build()
                .verify(token);

        return decodedJWT.getClaim("userId").asLong(); // Extract userId claim
    }

    public String extractEmail(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        // Handle "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secretKeyStatic))
                .build()
                .verify(token);
        return decodedJWT.getClaim("email").asString();
    }

    // Validate JWT Token
    public boolean validateToken(String token, String email) {
        String tokenEmail = getEmailFromToken(token);
        return email.equals(tokenEmail);
    }

    // Extract Email from Token
    public String getEmailFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        // Handle "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secretKeyStatic))
                .build()
                .verify(token);
        return decodedJWT.getSubject();
    }

    // Extract 'name' from Token
    public String getNameFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        // Handle "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secretKeyStatic))
                .build()
                .verify(token);
        return decodedJWT.getClaim("name").asString(); // Extract 'name' claim
    }
}