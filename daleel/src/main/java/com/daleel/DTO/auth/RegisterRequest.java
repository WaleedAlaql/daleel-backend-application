package com.daleel.DTO.auth;

import com.daleel.DTO.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Data Transfer Object for user registration requests.
 * 
 * This DTO validates and transfers new user registration data:
 * - Ensures all required fields are present
 * - Validates data format and constraints
 * - Enforces password security requirements
 * 
 * Security considerations:
 * - Password is never returned in responses
 * - Email must be unique in the system
 * - Implements UOH-specific validation rules
 * 
 * @see UserDTO
 * @version 1.0
 */
@Data
@Schema(description = "Registration Request - Contains data needed to register a new user")
public class RegisterRequest {

    /**
     * Full name of the new user.
     * Must contain only valid characters.
     */
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Name must contain only letters and basic punctuation")
    @Schema(description = "Full name of the user", example = "Mohammed Ahmed")
    private String name;

    /**
     * University email address.
     * Must be a valid UOH email that isn't already registered.
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
     * User's password.
     * Must meet security requirements:
     * - At least 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one number
     * - At least one special character
     */
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
        message = "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                 "one lowercase letter, one number and one special character"
    )
    @Schema(
        description = "User's password (must meet security requirements)",
        example = "SecureP@ss123"
    )
    private String password;

    /**
     * University student ID.
     * Must be a valid 9-digit UOH student ID.
     */
    @Pattern(
        regexp = "^\\d{9}$",
        message = "Student ID must be 9 digits"
    )
    @Schema(description = "UOH student ID number", example = "439010234")
    private String studentId;

    /**
     * User's department/college.
     * Must match one of the valid UOH departments.
     */
    @Pattern(
        regexp = "^(Computer Science|Information Technology|Engineering|Medicine|Business|Science)$",
        message = "Invalid department name"
    )
    @Schema(description = "User's department or college", example = "Computer Science")
    private String department;
}
