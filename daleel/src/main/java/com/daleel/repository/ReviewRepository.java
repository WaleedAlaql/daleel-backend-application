package com.daleel.repository;

import com.daleel.model.ProfessorReview;
import org.springframework.data.jpa.repository.JpaRepository;
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
    List<ProfessorReview> findByProfessorName(String professorName);
    
    /**
     * Finds all reviews for a specific course.
     * Used to show all professor reviews for a particular course.
     * 
     * @param courseCode The course code to find reviews for
     * @return List of reviews for the specified course
     * Usage example: reviewRepository.findByCourseCode("MATH101")
     */
    List<ProfessorReview> findByCourseCode(String courseCode);
}