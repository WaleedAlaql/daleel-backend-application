package com.daleel.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response.
 * Used to transfer authentication data (JWT token and user details) 
 * from the server to the client.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Authentication Response DTO")
public class AuthResponse {
    
    @Schema(description = "JWT token for authenticated user", 
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "User's full name", 
            example = "Mohammed Ahmed")
    private String name;

    @Schema(description = "User's email address", 
            example = "student@uoh.edu.sa")
    private String email;

    @Schema(description = "User's role", 
            example = "STUDENT")
    private String role;

    @Schema(description = "User's student ID", 
            example = "439010234")
    private String studentId;

    @Schema(description = "User's department", 
            example = "Computer Science")
    private String department;
}
