package com.daleel.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Material entity.
 * 
 * This class is used to transfer material data between the client and server,
 * providing a simplified view of the Material entity.
 * 
 * Fields:
 * - id: Unique identifier for the material
 * - title: Title of the material
 * - courseCode: Associated course code
 * - downloads: Number of times the material has been downloaded
 * 
 * @see Material
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Material Data Transfer Object")
public class MaterialDTO {

    /**
     * Unique identifier for the material.
     */
    private Long id;

    @NotBlank(message = "Title is required")
    @Schema(description = "Material title", example = "Calculus I Notes")
    private String title;

    @NotBlank(message = "Description is required")
    @Schema(description = "Material description", example = "Complete lecture notes for Calculus I")
    private String description;

    @NotBlank(message = "Course code is required")
    @Pattern(regexp = "^[A-Z]{2,4}\\d{3}$", message = "Invalid course code format")
    @Schema(description = "Course code", example = "MATH101")
    private String courseCode;

    @Schema(description = "Uploader's name", example = "Mohammed Ahmed")
    private String uploaderName;

    @Schema(description = "Download count", example = "42")
    private Integer downloads;

    @Schema(description = "Upload date")
    private LocalDateTime uploadDate;

    @Schema(description = "File type", example = "pdf")
    private String fileType;

    @Schema(description = "File size in bytes", example = "1048576")
    private Long fileSize;
}