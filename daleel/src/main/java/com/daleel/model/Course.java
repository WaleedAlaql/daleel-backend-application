package com.daleel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Course Entity - Represents academic courses for GPA calculation
 * This entity stores course information and grades, with methods
 * to calculate grade points according to UOH grading system
 */
@Data
@Entity
@Table(name = "courses")
public class Course {
    
    // Primary key configuration
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Course identification
    @NotBlank(message = "Course code is required")
    @Column(nullable = false)
    private String code;  // e.g., "MATH101"

    @NotBlank(message = "Course name is required")
    @Column(nullable = false)
    private String name;  // e.g., "Calculus I"

    // Credit hours (typically 1-6)
    @Min(value = 1, message = "Credit hours must be at least 1")
    @Max(value = 6, message = "Credit hours cannot exceed 6")
    @Column(nullable = false)
    private Integer creditHours;

    // Grade (can be null if course is in progress)
    @Column(nullable = true)
    private String grade;  // e.g., "A+", "B", etc.

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