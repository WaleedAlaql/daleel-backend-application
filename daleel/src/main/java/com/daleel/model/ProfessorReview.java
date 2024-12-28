package com.daleel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Professor Review Entity - Represents student reviews of professors
 * This entity stores ratings and feedback for professors, linked to specific courses
 * Includes validation to ensure quality and appropriate feedback
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "professor_reviews")
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
    @Pattern(regexp = "^[A-Z]{2,4}\\d{3}$", message = "Invalid course code format")
    @Column(nullable = false)
    private String courseCode;  // e.g., "MATH101"

    // Rating (1-5 stars)
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Column(nullable = false)
    private Integer rating;  // Numerical rating from 1 to 5

    // Review content
    @NotBlank(message = "Review text is required")
    @Size(min = 10, max = 500, message = "Review must be between 10 and 500 characters")
    @Column(nullable = false)
    private String reviewText;  // The actual review content

    // Timestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;  // When the review was created

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationship with User (Many reviews belong to one user)
    @ManyToOne(fetch = FetchType.LAZY)  // JPA: Lazy loading for better performance
    @JoinColumn(name = "user_id")
    private User user;  // The user who wrote the review

    // Lifecycle callback - Sets creation timestamp
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}