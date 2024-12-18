package com.daleel.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.daleel.exception.TokenException;
import java.security.Key;
import java.util.Date;

/**
 * Service for handling JWT operations including generation, validation, and parsing.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Generates a JWT token for a given email
     * @param email The user's email to be included in the token
     * @return The generated JWT token
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a JWT token and provides detailed error messages
     * @param token The full token string including "Bearer "
     * @throws TokenException with specific error message based on the validation failure
     */
    public void validateToken(String token) {
        try {
            // Check if token exists
            if (token == null || token.isEmpty()) {
                throw new TokenException("Token is missing");
            }

            // Check Bearer prefix and extract token
            if (!token.startsWith("Bearer ")) {
                throw new TokenException("Invalid token format. Token must start with 'Bearer '");
            }

            // Remove "Bearer " prefix and any whitespace
            String actualToken = token.substring(7).trim();

            // Validate token
            Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(actualToken);

        } catch (ExpiredJwtException e) {
            throw new TokenException("Token has expired");
        } catch (MalformedJwtException e) {
            throw new TokenException("Invalid token format");
        } catch (UnsupportedJwtException e) {
            throw new TokenException("Unsupported token type");
        } catch (IllegalArgumentException e) {
            throw new TokenException("Token claims string is empty");
        }
    }

    /**
     * Extracts the email from a valid JWT token
     * @param token The full token string including "Bearer "
     * @return The email stored in the token
     */
    public String getEmailFromToken(String token) {
        // Remove "Bearer " prefix and any whitespace
        String actualToken = token.substring(7).trim();
        
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(actualToken)
                .getBody()
                .getSubject();
    }

    /**
     * Creates the signing key from the secret
     * @return The signing key used for JWT operations
     */
    private Key getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 