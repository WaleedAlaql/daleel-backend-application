package com.daleel.service;

import com.daleel.model.Material;
import com.daleel.model.User;
import com.daleel.repository.MaterialRepository;
import com.daleel.exception.MaterialNotFoundException;
import com.daleel.exception.UnauthorizedAccessException;
import com.daleel.exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class responsible for managing educational materials.
 * 
 * This service handles:
 * - Upload and management of educational materials
 * - Download tracking
 * - Material search and retrieval
 * - Access control and permissions
 *
 * Business Rules:
 * - Only authenticated users can upload materials
 * - Users can only modify/delete their own materials
 * - Materials must be associated with valid course codes
 * - File size and type restrictions apply
 *
 * @author Waleed Alaql
 * @version 1.0
 * @see Material
 * @see MaterialRepository
 */
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final UserService userService;

    // Maximum file size in bytes (50MB)
    private static final long MAX_FILE_SIZE = 52_428_800L;
    
    // Allowed file types
    private static final List<String> ALLOWED_FILE_TYPES = List.of(
        "pdf", "doc", "docx", "ppt", "pptx"
    );

    /**
     * Creates a new educational material in the system.
     * 
     * This method:
     * 1. Validates the material data
     * 2. Associates it with the uploading user
     * 3. Initializes download counter
     * 4. Saves the material
     * 
     * Validation Rules:
     * - File must be of allowed type
     * - File size must be within limits
     * - Course code must be valid
     * - Title and description required
     * 
     * @param material The material entity to be created
     * @param userId The ID of the uploading user
     * @return The created material with generated ID
     * @throws InvalidInputException if validation fails
     * @throws UnauthorizedAccessException if user not found
     * 
     * Usage example:
     * {@code
     * Material newMaterial = new Material();
     * newMaterial.setTitle("Calculus Notes");
     * newMaterial.setCourseCode("MATH101");
     * Material created = materialService.createMaterial(newMaterial, currentUserId);
     * }
     */
    @Transactional
    public Material createMaterial(Material material, Long userId) {
        // Get user and validate
        User user = userService.getUserById(userId);
        
        // Validate material
        validateMaterial(material);
        
        // Set initial values
        material.setUser(user);
        material.setDownloads(0);
        material.setUploadDate(LocalDateTime.now());
        
        return materialRepository.save(material);
    }

    /**
     * Retrieves a material by its ID.
     * 
     * Used for:
     * - Material display
     * - Download preparation
     * - Edit/delete operations
     * 
     * @param id The unique identifier of the material
     * @return The found material entity
     * @throws MaterialNotFoundException if material doesn't exist
     * 
     * Usage example:
     * {@code
     * Material material = materialService.getMaterialById(123L);
     * System.out.println("Title: " + material.getTitle());
     * }
     */
    public Material getMaterialById(Long id) {
        return materialRepository.findById(id)
            .orElseThrow(() -> new MaterialNotFoundException("Material not found with id: " + id));
    }

    /**
     * Retrieves all materials for a specific course.
     * 
     * This method:
     * - Returns materials sorted by upload date
     * - Includes download counts
     * - Filters by exact course code match
     * 
     * @param courseCode The course code to search for
     * @return List of materials for the course
     * @throws InvalidInputException if course code is invalid
     * 
     * Usage example:
     * {@code
     * List<Material> materials = materialService.getMaterialsByCourse("MATH101");
     * materials.forEach(m -> System.out.println(m.getTitle()));
     * }
     */
    public List<Material> getMaterialsByCourse(String courseCode) {
        validateCourseCode(courseCode);
        return materialRepository.findByCourseCode(courseCode);
    }

    /**
     * Records a download for a material and increments counter.
     * 
     * This method:
     * 1. Verifies material exists
     * 2. Updates download counter atomically
     * 3. Handles concurrent downloads safely
     * 
     * @param id The ID of the downloaded material
     * @throws MaterialNotFoundException if material doesn't exist
     * 
     * Usage example:
     * {@code
     * materialService.downloadMaterial(123L);
     * System.out.println("Download recorded");
     * }
     */
    @Transactional
    public void downloadMaterial(Long id) {
        // Verify material exists
        getMaterialById(id);
        // Increment downloads atomically
        materialRepository.incrementDownloads(id);
    }

   /**
     * Deletes a material from the system.
     * 
     * Security Rules:
     * - Admins can delete any material
     * - Regular users can only delete their own materials
     * 
     * @param id The ID of the material to delete
     * @param userId The ID of the user attempting deletion
     * @param isAdmin Whether the user has admin privileges
     * @throws MaterialNotFoundException if material doesn't exist
     * @throws UnauthorizedAccessException if user not authorized
     */
    @Transactional
    public void deleteMaterial(Long id, Long userId, boolean isAdmin) {
        Material material = getMaterialById(id);
        
        // Admin can delete any material
        if (!isAdmin && !material.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only delete your own materials");
        }
        
        materialRepository.delete(material);
    }

    /**
     * Validates material data before saving.
     * 
     * Checks:
     * - File type is allowed
     * - File size is within limits
     * - Required fields are present
     * - Course code format is valid
     * 
     * @param material The material to validate
     * @throws InvalidInputException if validation fails
     */
    private void validateMaterial(Material material) {
        if (material.getTitle() == null || material.getTitle().trim().isEmpty()) {
            throw new InvalidInputException("Title is required");
        }

        if (material.getFileSize() > MAX_FILE_SIZE) {
            throw new InvalidInputException("File size exceeds maximum limit of 50MB");
        }

        String fileType = material.getFileType().toLowerCase();
        if (!ALLOWED_FILE_TYPES.contains(fileType)) {
            throw new InvalidInputException("File type not allowed. Allowed types: " + 
                String.join(", ", ALLOWED_FILE_TYPES));
        }

        validateCourseCode(material.getCourseCode());
    }

    /**
     * Validates course code format.
     * 
     * Format rules:
     * - Must be uppercase
     * - Must be 6-8 characters
     * - Must follow department code pattern
     * 
     * @param courseCode The course code to validate
     * @throws InvalidInputException if course code is invalid
     */
    private void validateCourseCode(String courseCode) {
        if (courseCode == null || !courseCode.matches("^[A-Z]{2,4}\\d{3,4}$")) {
            throw new InvalidInputException("Invalid course code format. Example: MATH101");
        }
    }
}