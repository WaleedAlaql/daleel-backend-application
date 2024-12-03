package com.daleel.repository;

import com.daleel.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Course entity operations.
 * Manages all database operations related to courses.
 * Inherits basic CRUD operations from JpaRepository.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    /**
     * Finds courses by their course code.
     * Used to look up specific courses in the system.
     * 
     * @param code The course code to search for (e.g., "MATH101")
     * @return List of courses matching the code
     * Usage example: courseRepository.findByCode("MATH101")
     */
    List<Course> findByCode(String code);
}