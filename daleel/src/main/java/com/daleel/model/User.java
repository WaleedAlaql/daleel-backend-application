package com.daleel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import com.daleel.security.enums.Role;

/**
 * User Entity - Represents a student in the system
 * This entity stores all user-related information and manages relationships
 * with other entities like materials and reviews
 */
@Data                        // Lombok: Generates getters, setters, toString, equals, and hashCode
@Entity                      // JPA: Marks this class as a JPA entity
@Table(name = "users")       // JPA: Specifies the table name in the database
@NoArgsConstructor           // Lombok: Generates a no-args constructor
@AllArgsConstructor          // Lombok: Generates an all-args constructor
@Builder                     // Lombok: Generates a builder for the class   
public class User {
    
    // Primary key configuration
    @Id// JPA: Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // JPA: Auto-increment
    private Long id;

    // Email field with validation
    @NotBlank(message = "Email is required")  // Validation: Ensures email isn't blank
    @Email(message = "Please provide a valid email") // Validation: Ensures valid email format
    @Pattern(
        regexp = ".*@uoh\\.edu\\.sa$", 
        message = "Must be a UOH email address"
    ) // Validation: Ensures email is from UOH domain
    @Column(unique = true, nullable = false) // JPA: Unique constraint and not null
    private String email;

    // Password field
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password; // Note: This will store the hashed password, not plain text

    // User's full name
    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    // User's student ID
    @NotBlank(message = "Student ID is required")
    @Column(nullable = false)
    private String studentId;

    // User's department
    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;

    // User's role
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;



    // Account creation timestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Account status
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // Relationships
    
    // One-to-Many relationship with Materials
    @OneToMany(
        mappedBy = "user",         // JPA: Specifies the field in Material entity that owns the relationship
        cascade = CascadeType.ALL  // JPA: When user is deleted, delete all their materials
    )
    private List<Material> materials;

    // One-to-Many relationship with Reviews
    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL
    )
    private List<ProfessorReview> reviews;

    // Lifecycle callback - Automatically sets creation timestamp
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}