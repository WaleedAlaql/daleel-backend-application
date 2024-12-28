package com.daleel.repository;

import com.daleel.model.Course;
import com.daleel.enums.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Course entity operations.
 * Handles all database operations related to courses.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    /**
     * Find a course by its code.
     * 
     * @param code The course code (e.g., "MATH101")
     * @return Optional containing the course if found
     */
    Optional<Course> findByCourseCode(String courseCode);

    /**
     * Find all courses in a specific department.
     * 
     * @param department The department name
     * @return List of courses in the department
     */
    List<Course> findByDepartment(Department department);

    /**
     * Check if a course code already exists.
     * 
     * @param code The course code to check
     * @return true if the code exists
     */
    boolean existsByCourseCode(String courseCode);

    /**
     * Calculate GPA for a list of courses.
     * 
     * @return The calculated GPA
     */
    @Query("SELECT SUM(c.creditHours * CASE c.grade " +
           "WHEN 'A+' THEN 4.00 " +
           "WHEN 'A' THEN 3.75 " +
           "WHEN 'B+' THEN 3.50 " +
           "WHEN 'B' THEN 3.00 " +
           "WHEN 'C+' THEN 2.50 " +
           "WHEN 'C' THEN 2.00 " +
           "WHEN 'D+' THEN 1.50 " +
           "WHEN 'D' THEN 1.00 " +
           "ELSE 0.00 END) / SUM(c.creditHours) " +
           "FROM Course c WHERE c.id IN :courseIds")
    Double calculateGPA(@Param("courseIds") List<Long> courseIds);

    /**
     * Find courses by department and partial name match.
     * 
     * @param department The department name
     * @param name The partial course name
     * @return List of matching courses
     */
    List<Course> findByDepartmentAndCourseName(Department department, String courseName);
}