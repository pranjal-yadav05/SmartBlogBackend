package com.smartblogbackend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.smartblogbackend.model.User;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey; // Store this securely (environment variable or secure vault)

    // Generate JWT Token from User object
    public String generateJwtToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withSubject(user.getEmail()) // Use user's email as the subject
                .withClaim("name", user.getName())
                .withClaim("email", user.getEmail())// Add name as a claim
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour expiration
                .sign(algorithm);
    }

    // Validate JWT Token
    public boolean validateToken(String token, String email) {
        String tokenEmail = getEmailFromToken(token);
        return email.equals(tokenEmail);
    }

    // Extract Email from Token
    public String getEmailFromToken(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secretKey))
                .build()
                .verify(token);
        return decodedJWT.getSubject();
    }

    // Extract 'name' from Token
    public String getNameFromToken(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secretKey))
                .build()
                .verify(token);
        return decodedJWT.getClaim("name").asString(); // Extract 'name' claim
    }
}
