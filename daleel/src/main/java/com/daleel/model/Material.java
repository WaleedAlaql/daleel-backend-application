package com.daleel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Material Entity - Represents educational materials uploaded by users
 * This entity stores information about uploaded files, including metadata
 * and download statistics
 */
@Data
@Entity
@Table(name = "materials")
public class Material {
    
    // Primary key configuration
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Course information
    @NotBlank(message = "Course code is required")
    @Column(nullable = false)
    private String courseCode;  // e.g., "MATH101"

    @NotBlank(message = "Course name is required")
    @Column(nullable = false)
    private String courseName;  // e.g., "Calculus I"

    // Material details
    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;  // Title of the material

    // File information
    @Column(nullable = false)
    private String fileUrl;  // URL/path to the stored file

    @Column(nullable = false)
    private LocalDateTime uploadDate;  // When the material was uploaded

    @Column(nullable = false)
    private Integer downloads = 0;  // Download counter

    @Column(nullable = false)
    private String fileType = "PDF";  // Type of file (defaulting to PDF)

    @Column(nullable = false)
    private Long fileSize;  // Size of file in bytes

    // Relationship with User (Many materials belong to one user)
    @ManyToOne(fetch = FetchType.LAZY)  // JPA: Lazy loading for better performance
    @JoinColumn(
        name = "user_id",  // Name of the foreign key column
        nullable = false   // User is required
    )
    private User user;

    // Lifecycle callback - Sets initial values
    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
        if (downloads == null) downloads = 0;
    }
}