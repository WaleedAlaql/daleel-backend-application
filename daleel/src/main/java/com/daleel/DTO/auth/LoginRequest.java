package com.daleel.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Data Transfer Object for user login requests.
 * 
 * This DTO handles user authentication attempts:
 * - Validates login credentials format
 * - Transfers login data securely
 * - Supports UOH email format
 * 
 * Security considerations:
 * - Password is never logged or returned
 * - Implements rate limiting (at service level)
 * - Supports account locking (at service level)
 * 
 * @see UserDTO
 * @version 1.0
 */
@Data
@Schema(description = "Login Request - Contains credentials for user authentication")
public class LoginRequest {

    /**
     * User's email address for login.
     * Must be a valid UOH email format.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@uoh\\.edu\\.sa$",
        message = "Must be a valid UOH email address"
    )
    @Schema(description = "UOH email address", example = "student@uoh.edu.sa")
    private String email;

    /**
     * User's password for authentication.
     * Not validated for format here (only presence),
     * actual validation happens during authentication.
     */
    @NotBlank(message = "Password is required")
    @Schema(
        description = "User's password",
        example = "SecureP@ss123"
    )
    private String password;
}
