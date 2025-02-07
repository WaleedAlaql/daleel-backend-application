package com.daleel.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for User entity.
 * 
 * This DTO is used to:
 * - Transfer user data between client and server
 * - Validate user input for updates
 * - Hide sensitive information (e.g., password)
 * 
 * Validation rules:
 * - Name must not be blank
 * - Email must be a valid UOH email address
 * - Department is optional but must be valid if provided
 * - StudentId must follow UOH format
 * 
 * Usage examples:
 * - User profile display
 * - User information updates
 * - User listings
 * 
 * @see User
 * @version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User Data Transfer Object - Represents user information for API operations")
public class UserDTO {

    /**
     * Unique identifier for the user.
     * Generated by the system, read-only for clients.
     */
    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    /**
     * Full name of the user.
     * Required field with length constraints.
     */
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Name must contain only letters and basic punctuation")
    @Schema(description = "Full name of the user", example = "Mohammed Ahmed")
    private String name;

    /**
     * University email address.
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
     * User's department/college.
     * Optional field with predefined valid values.
     */
    @Pattern(
        regexp = "^(Computer Science|Information Technology|Engineering|Medicine|Business|Science)?$",
        message = "Invalid department name"
    )
    @Schema(description = "User's department or college", example = "Computer Science")
    private String department;

    /**
     * University student ID.
     * Must follow UOH student ID format.
     */
    @Pattern(
        regexp = "^\\d{9}$",
        message = "Student ID must be 9 digits"
    )
    @Schema(description = "UOH student ID number", example = "439010234")
    private String studentId;

    /**
     * User's role in the system.
     * Read-only field, set by the system.
     */
    @Schema(description = "User's role in the system", example = "STUDENT")
    private String role;
}