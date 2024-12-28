package com.daleel.repository;

import com.daleel.model.ProfessorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for ProfessorReview entity operations.
 * Manages all database operations related to professor reviews.
 * Inherits basic CRUD operations from JpaRepository.
 */
@Repository
public interface ReviewRepository extends JpaRepository<ProfessorReview, Long> {
    
    /**
     * Retrieves all reviews for a specific professor.
     * Returns reviews sorted by default repository order.
     * 
     * @param professorName The name of the professor to find reviews for
     * @return List of reviews for the specified professor
     * Usage example: reviewRepository.findByProfessorName("Dr. Smith")
     */
    List<ProfessorReview> findByProfessorNameContainingIgnoreCase(String professorName);
    
    /**
     * Finds all reviews for a specific course.
     * Used to show all professor reviews for a particular course.
     * 
     * @param courseCode The course code to find reviews for
     * @return List of reviews for the specified course
     * Usage example: reviewRepository.findByCourseCode("MATH101")
     */
    List<ProfessorReview> findByCourseCode(String courseCode);
    
    /**
     * Finds all reviews for a specific user.
     * Used to show all professor reviews for a particular user.
     * 
     * @param userEmail The email of the user to find reviews for
     * @return List of reviews for the specified user
     * Usage example: reviewRepository.findByUserEmail("john@example.com")
     */
    List<ProfessorReview> findByUserEmail(String userEmail);
    
    /**
     * Gets the average rating for a specific professor.
     * Used to show the average rating for a particular professor.
     * 
     * @param professorName The name of the professor to get the average rating for
     * @return The average rating for the specified professor
     * Usage example: reviewRepository.getAverageRatingForProfessor("Dr. Smith")
     */
    @Query("SELECT AVG(r.rating) FROM ProfessorReview r WHERE r.professorName = :professorName")
    Double getAverageRatingForProfessor(@Param("professorName") String professorName);
    
    /**
     * Checks if a review exists for a specific user and course.
     * Used to check if a user has already reviewed a particular course.
     * 
     * @param userEmail The email of the user to check for
     * @param courseCode The course code to check for
     * @return True if a review exists for the specified user and course, false otherwise
     * Usage example: reviewRepository.existsByUserEmailAndCourseCode("john@example.com", "MATH101")
     */
    boolean existsByUserEmailAndCourseCode(String userEmail, String courseCode);
}