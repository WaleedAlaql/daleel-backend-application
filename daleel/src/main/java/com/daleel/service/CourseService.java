package com.daleel.service;

import com.daleel.model.Course;
import com.daleel.repository.CourseRepository;
import com.daleel.exception.CourseNotFoundException;
import com.daleel.exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service class responsible for managing courses and GPA calculations.
 * 
 * This service handles:
 * - Course management (create, retrieve, update)
 * - GPA calculations (4.0 scale)
 * - Grade validation and conversion
 * - Course code validation
 * 
 * 
 * @author Waleed Alaql
 * @version 1.0
 * @see Course
 * @see CourseRepository
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    // Grade points mapping (4.0 scale)
    private static final Map<String, Double> GRADE_POINTS = Map.of(
        "A+", 4.00,
        "A",  3.75,
        "B+", 3.50,
        "B",  3.00,
        "C+", 2.50,
        "C",  2.00,
        "D+", 1.50,
        "D",  1.00,
        "F",  0.00
    );

    /**
     * Retrieves a course by its ID.
     * 
     * @param id The course ID
     * @return The found course
     * @throws CourseNotFoundException if course doesn't exist
     */
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + id));
    }

    /**
     * Retrieves courses by their code.
     * 
     * @param code The course code (e.g., "MATH101")
     * @return List of matching courses
     */
    public List<Course> getCoursesByCode(String code) {
        validateCourseCode(code);
        return courseRepository.findByCode(code);
    }

    /**
     * Creates a new course.
     * 
     * @param course The course to create
     * @return The created course
     * @throws InvalidInputException if course data is invalid
     */
    @Transactional
    public Course createCourse(Course course) {
        validateCourse(course);
        return courseRepository.save(course);
    }

    /**
     * Updates a course's grade.
     * 
     * @param id The course ID
     * @param grade The new grade
     * @return The updated course
     * @throws CourseNotFoundException if course doesn't exist
     * @throws InvalidInputException if grade is invalid
     */
    @Transactional
    public Course updateCourseGrade(Long id, String grade) {
        validateGrade(grade);
        Course course = getCourseById(id);
        course.setGrade(grade);
        return courseRepository.save(course);
    }

    /**
     * Calculates GPA for a list of courses.
     * 
     * @param courses List of courses
     * @return GPA on 4.0 scale
     */
    public double calculateGPA(List<Course> courses) {
        double totalPoints = 0;
        int totalCredits = 0;

        for (Course course : courses) {
            if (course.getGrade() != null && GRADE_POINTS.containsKey(course.getGrade())) {
                double points = GRADE_POINTS.get(course.getGrade());
                totalPoints += points * course.getCreditHours();
                totalCredits += course.getCreditHours();
            }
        }

        if (totalCredits == 0) {
            return 0.0;
        }

        // Round to 2 decimal places
        double gpa = totalPoints / totalCredits;
        return Math.round(gpa * 100.0) / 100.0;
    }

    /**
     * Gets letter grade for GPA value.
     * 
     * @param gpa GPA value
     * @return Letter grade
     */
    public String getLetterGrade(double gpa) {
        if (gpa >= 4.00) return "A+";
        if (gpa >= 3.50) return "B+";
        if (gpa >= 3.00) return "B";
        if (gpa >= 2.50) return "C+";
        if (gpa >= 2.00) return "C";
        if (gpa >= 1.50) return "D+";
        if (gpa >= 1.00) return "D";
        return "F";
    }

    /**
     * Gets grade points for a letter grade.
     * 
     * @param grade Letter grade
     * @return Grade points on 4.0 scale
     */
    public double getGradePoints(String grade) {
        validateGrade(grade);
        return GRADE_POINTS.get(grade);
    }

    /**
     * Validates course data.
     * 
     * @param course The course to validate
     * @throws InvalidInputException if validation fails
     */
    private void validateCourse(Course course) {
        validateCourseCode(course.getCode());
        
        if (course.getCreditHours() < 1 || course.getCreditHours() > 4) {
            throw new InvalidInputException("Credit hours must be between 1 and 4");
        }
        
        if (course.getName() == null || course.getName().trim().isEmpty()) {
            throw new InvalidInputException("Course name is required");
        }
    }

    /**
     * Validates course code format.
     * 
     * @param code The course code to validate
     * @throws InvalidInputException if code is invalid
     */
    private void validateCourseCode(String code) {
        if (code == null || !code.matches("^[A-Z]{2,4}\\d{3,4}$")) {
            throw new InvalidInputException("Invalid course code format. Example: MATH101");
        }
    }

    /**
     * Validates grade value.
     * 
     * @param grade The grade to validate
     * @throws InvalidInputException if grade is invalid
     */
    private void validateGrade(String grade) {
        if (grade != null && !GRADE_POINTS.containsKey(grade)) {
            throw new InvalidInputException("Invalid grade. Allowed grades: " + 
                String.join(", ", GRADE_POINTS.keySet()));
        }
    }

    /**
     * Gets all allowed grades.
     * 
     * @return Set of valid grades
     */
    public Set<String> getAllowedGrades() {
        return GRADE_POINTS.keySet();
    }
}