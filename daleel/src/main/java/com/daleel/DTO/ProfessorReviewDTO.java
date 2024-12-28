package com.daleel.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for ProfessorReview entity.
 * 
 * This class is used to transfer review data between the client and server,
 * providing a simplified view of the ProfessorReview entity.
 * 
 * Fields:
 * - id: Unique identifier for the review
 * - professorName: Name of the professor being reviewed
 * - rating: Rating given to the professor
 * - reviewText: Text of the review
 * 
 * @see ProfessorReview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorReviewDTO {

    /**
     * Unique identifier for the review.
     */
    @Schema(description = "Unique identifier for the review")
    private Long id;

    /**
     * Name of the professor being reviewed.
     */
    @NotBlank(message = "Professor name is required")
    @Schema(description = "Professor name", example = "Dr. John Doe")
    private String professorName;

    /**
     * Course code of the course being reviewed.
     */
    @NotBlank(message = "Course code is required")
    @Pattern(regexp = "^[A-Z]{2,4}\\d{3}$", message = "Invalid course code format")
    @Schema(description = "Course code", example = "CS101")
    private String courseCode;

    /**
     * Rating given to the professor.
     */
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Schema(description = "Rating (1-5)", example = "4")
    private Integer rating;

    /**
     * Text of the review.
     */
    @NotBlank(message = "Review text is required")
    @Size(min = 10, max = 500, message = "Review must be between 10 and 500 characters")
    @Schema(description = "Review text", example = "Great professor, explains concepts clearly")
    private String reviewText;

    /**
     * Creation timestamp.
     */
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    /**
     * Reviewer's Name.
     */
    @Schema(description = "Reviewer's Name")
    private String userName;
}
