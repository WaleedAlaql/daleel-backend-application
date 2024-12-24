package com.daleel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Material Entity - Represents educational materials in the system.
 * 
 * This entity stores:
 * - Material metadata (title, description, course info)
 * - File information (URL, type, size)
 * - Upload details (date, user)
 * - Usage statistics (download count)
 * 
 * Relationships:
 * - ManyToOne with User (uploader)
 * 
 * Business Rules:
 * - Materials must be associated with a valid course code
 * - Materials must have a title and description
 * - File information is required
 * - Download count starts at 0
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "materials")
public class Material {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(nullable = false)
    private String description;

    @NotBlank(message = "Course code is required")
    @Pattern(regexp = "^[A-Z]{2,4}\\d{3}$", message = "Invalid course code format")
    @Column(nullable = false)
    private String courseCode;

    @NotBlank(message = "Course name is required")
    @Column(nullable = false)
    private String courseName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "File URL is required")
    @Column(nullable = false)
    private String fileUrl;

    @NotBlank(message = "File type is required")
    @Column(nullable = false)
    private String fileType;

    @Positive(message = "File size must be positive")
    @Column(nullable = false)
    private Long fileSize;

    @PositiveOrZero(message = "Downloads cannot be negative")
    @Column(nullable = false)
    @Builder.Default
    private Integer downloads = 0;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
}