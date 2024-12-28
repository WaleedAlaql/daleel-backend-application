package com.daleel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.daleel.enums.Department;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Course Entity - Represents academic courses for GPA calculation
 * This entity stores course information and grades, with methods
 * to calculate grade points according to UOH grading system
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "courses")
public class Course {
    
    // Primary key configuration
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Course identification
    @NotBlank(message = "Course code is required")
    @Pattern(regexp = "^[A-Z]{2,4}\\d{3}$", message = "Invalid course code format")
    @Column(name = "course_code", nullable = false, unique = true)
    private String courseCode;  // e.g., "MATH101"

    @NotBlank(message = "Course name is required")
    @Column(name = "course_name", nullable = false)
    private String courseName;

    // Credit hours (typically 1-6)
    @Min(value = 1, message = "Credit hours must be between 1 and 6")
    @Max(value = 6, message = "Credit hours must be between 1 and 6")
    @Column(name = "credit_hours", nullable = false)
    private Integer creditHours;

    // Grade (can be null if course is in progress)
    @Pattern(regexp = "^(A\\+|A|B\\+|B|C\\+|C|D\\+|D|F)$", message = "Invalid grade")
    @Column(name = "grade")
    private String grade;  // e.g., "A+", "B", etc.

    @Column(name = "department", nullable = false)
    @Enumerated(EnumType.STRING)
    private Department department;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Material> materials;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Calculates grade points based on UOH grading system
     * A+ = 4.0, A = 3.75, B+ = 3.5, etc.
     *
     * @return Double value of grade points or null if grade not set
     */
    public Double getGradePoints() {
        if (grade == null) return null;
        
        // Convert grade to grade points using UOH scale
        return switch (grade.toUpperCase()) {
            case "A+" -> 4.0;    // Exceptional
            case "A"  -> 3.75;   // Excellent
            case "B+" -> 3.5;    // Superior
            case "B"  -> 3.0;    // Very Good
            case "C+" -> 2.5;    // Above Average
            case "C"  -> 2.0;    // Good
            case "D+" -> 1.5;    // High Pass
            case "D"  -> 1.0;    // Pass
            case "F"  -> 0.0;    // Fail
            default   -> null;   // Invalid grade
        };
    }
}