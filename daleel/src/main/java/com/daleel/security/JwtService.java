package com.daleel.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.daleel.exception.TokenException;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.Claims;
import java.util.Base64;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling JWT operations including generation, validation, and parsing.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @PostConstruct
    protected void init() {
        // Encode secret key if not already in Base64
        try {
            Decoders.BASE64.decode(secretKey);
        } catch (IllegalArgumentException e) {
            // If not valid Base64, encode it
            secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
            log.info("Secret key has been Base64 encoded");
        }
    }

    /**
     * Generates a JWT token for a given email
     * @param email The user's email to be included in the token
     * @return The generated JWT token
     */
    public String generateToken(String email) {
        try {
            return Jwts.builder()
                    .setSubject(email)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("Error generating token: {}", e.getMessage());
            throw new RuntimeException("Could not generate token", e);
        }
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
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Error creating signing key: {}", e.getMessage());
            throw new IllegalStateException("Invalid secret key");
        }
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username != null && 
                   username.equals(userDetails.getUsername()) && 
                   !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            throw e;
        }
    }
} 