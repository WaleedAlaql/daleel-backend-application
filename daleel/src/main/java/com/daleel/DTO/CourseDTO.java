package com.daleel.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import com.daleel.enums.Department;
/**
 * Data Transfer Object for Course entity.
 * 
 * This class is used to transfer course data between the client and server,
 * providing a simplified view of the Course entity.
 * 
 * Fields:
 * - id: Unique identifier for the course
 * - code: Course code (e.g., "MATH101")
 * - name: Name of the course
 * - creditHours: Number of credit hours for the course
 * - grade: Grade received in the course
 * 
 * @see Course
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course Data Transfer Object")
public class CourseDTO {

    @Schema(description = "Course ID")
    private Long id;

    @NotBlank(message = "Course code is required")
    @Pattern(regexp = "^[A-Z]{2,4}\\d{3}$", message = "Invalid course code format")
    @Schema(description = "Course code", example = "MATH101")
    private String courseCode;

    @NotBlank(message = "Course name is required")
    @Schema(description = "Course name", example = "Calculus I")
    private String courseName;

    @Min(value = 1)
    @Max(value = 6)
    @Schema(description = "Credit hours", example = "3")
    private Integer creditHours;

    @Pattern(regexp = "^(A\\+|A|B\\+|B|C\\+|C|D\\+|D|F)$", message = "Invalid grade")
    @Schema(description = "Grade received", example = "A+")
    private String grade;

    @NotNull(message = "Department is required")
    @Schema(description = "Department", example = "COMPUTER_SCIENCE", 
           allowableValues = {"MATHEMATICS", "COMPUTER_SCIENCE", "PHYSICS", "CHEMISTRY", 
                             "BIOLOGY", "ENGINEERING", "BUSINESS", "MEDICINE"})
    private Department department;

    @Schema(description = "Material count", example = "10")
    private Integer materialCount;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}