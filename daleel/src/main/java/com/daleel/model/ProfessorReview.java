package com.daleel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Professor Review Entity - Represents student reviews of professors
 * This entity stores ratings and feedback for professors, linked to specific courses
 * Includes validation to ensure quality and appropriate feedback
 */
@Data                                   // Lombok: Generates getters, setters, toString, equals, and hashCode
@Entity                                 // JPA: Marks this class as a JPA entity
@Table(name = "professor_reviews")      // JPA: Specifies the database table name
public class ProfessorReview {
    
    // Primary key configuration
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Professor information
    @NotBlank(message = "Professor name is required")
    @Column(nullable = false)
    private String professorName;  // Name of the professor being reviewed

    // Course information
    @NotBlank(message = "Course code is required")
    @Column(nullable = false)
    private String courseCode;  // e.g., "MATH101"

    // Rating (1-5 stars)
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Column(nullable = false)
    private Integer rating;  // Numerical rating from 1 to 5

    // Review content
    @NotBlank(message = "Review text is required")
    @Column(nullable = false, length = 1000)  // Limits text to 1000 characters
    private String reviewText;  // The actual review content

    // Timestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;  // When the review was created

    // Relationship with User (Many reviews belong to one user)
    @ManyToOne(fetch = FetchType.LAZY)  // JPA: Lazy loading for better performance
    @JoinColumn(
        name = "user_id",    // Name of the foreign key column
        nullable = false     // User is required
    )
    private User user;  // The user who wrote the review

    // Lifecycle callback - Sets creation timestamp
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}