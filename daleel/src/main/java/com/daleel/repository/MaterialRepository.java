package com.daleel.repository;

import com.daleel.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Material entity operations.
 * Handles all database operations related to educational materials.
 * Inherits basic CRUD operations from JpaRepository.
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    
    /**
     * Retrieves all materials for a specific course.
     * Materials are returned based on the course code (e.g., "MATH101").
     * 
     * @param courseCode The course code to search for
     * @return List of materials for the specified course
     * Usage example: materialRepository.findByCourseCode("MATH101")
     */
    List<Material> findByCourseCode(String courseCode);
    
    /**
     * Finds all materials uploaded by a specific user.
     * Used to display a user's contributions.
     * 
     * @param userId The ID of the user whose materials to find
     * @return List of materials uploaded by the user
     * Usage example: materialRepository.findByUserId(userId)
     */
    List<Material> findByUserId(Long userId);
    
    /**
     * Increments the download counter for a specific material.
     * Uses a custom query to update only the downloads field.
     * 
     * @param id The ID of the material to update
     * @Modifying indicates this query modifies the database
     * Usage example: materialRepository.incrementDownloads(materialId)
     */
    @Modifying
    @Query("UPDATE Material m SET m.downloads = m.downloads + 1 WHERE m.id = :id")
    void incrementDownloads(Long id);
}