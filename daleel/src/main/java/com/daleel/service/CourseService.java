package com.daleel.service;

import com.daleel.DTO.CourseDTO;
import com.daleel.enums.Department;
import com.daleel.model.Course;
import com.daleel.repository.CourseRepository;
import com.daleel.exception.CourseNotFoundException;
import com.daleel.exception.DuplicateCourseException;
import com.daleel.exception.InvalidCourseDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.daleel.repository.UserRepository;
import com.daleel.model.User;

/**
 * Service class for managing courses.
 * Handles business logic for course operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * Create a new course.
     *
     * @param courseDTO The course data
     * @return The created course
     * @throws DuplicateCourseException if course code already exists
     */
    @Transactional
    public CourseDTO createCourse(CourseDTO courseDTO) {
        validateCourse(courseDTO);
        log.info("Creating new course with code: {}", courseDTO.getCourseCode());
        
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (courseRepository.existsByCourseCode(courseDTO.getCourseCode())) {
                log.error("Course with code {} already exists", courseDTO.getCourseCode());
                throw new DuplicateCourseException("Course with code " + courseDTO.getCourseCode() + " already exists");
            }

            Course course = Course.builder()
                .courseCode(courseDTO.getCourseCode())
                .courseName(courseDTO.getCourseName())
                .creditHours(courseDTO.getCreditHours())
                .grade(courseDTO.getGrade())
                .department(courseDTO.getDepartment())
                .user(currentUser)  // Set the course owner
                .build();

            Course savedCourse = courseRepository.save(course);
            log.info("Created course with ID: {}", savedCourse.getId());
            
            return convertToDTO(savedCourse);
        } catch (Exception e) {
            log.error("Error creating course: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create course: " + e.getMessage(), e);
        }
    }

    /**
     * Get a course by its ID.
     *
     * @param id Course ID
     * @return The course data
     * @throws CourseNotFoundException if course not found
     */
    public CourseDTO getCourseById(Long id) {
        log.info("Fetching course with ID: {}", id);
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Course not found with ID: {}", id);
                return new CourseNotFoundException("Course not found with ID: " + id);
            });
        return convertToDTO(course);
    }

    /**
     * Get a course by its code.
     *
     * @param code Course code
     * @return The course data
     * @throws CourseNotFoundException if course not found
     */
    public CourseDTO getCourseByCode(String code) {
        log.info("Fetching course with code: {}", code);
        Course course = courseRepository.findByCourseCode(code)
            .orElseThrow(() -> {
                log.error("Course not found with code: {}", code);
                return new CourseNotFoundException("Course not found with code: " + code);
            });
        return convertToDTO(course);
    }

    /**
     * Get all courses.
     *
     * @return List of all courses
     */
    public List<CourseDTO> getAllCourses() {
        log.info("Fetching all courses");
        return courseRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get courses by department.
     *
     * @param department Department name
     * @return List of courses in the department
     */
    public List<CourseDTO> getCoursesByDepartment(String department) {
        log.info("Fetching courses for department: {}", department);
        return courseRepository.findByDepartment(Department.valueOf(department)).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Update a course.
     *
     * @param id Course ID
     * @param courseDTO Updated course data
     * @return Updated course
     * @throws CourseNotFoundException if course not found
     */
    @Transactional
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        log.debug("Request to update Course with ID: {}", id);
        
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        log.debug("Current user attempting update: {}", currentUserEmail);
        
        // Find the course
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Course not found with id: {}", id);
                return new CourseNotFoundException("Course not found with id: " + id);
            });
        
        // Log course owner information
        log.debug("Course owner: {}", course.getUser() != null ? course.getUser().getEmail() : "no owner");
        log.debug("User roles: {}", auth.getAuthorities());
        
        // Check if user has permission to update this course
        if (!isUserAuthorizedToUpdateCourse(course, currentUserEmail)) {
            log.error("User {} not authorized to update course {}", currentUserEmail, id);
            throw new AccessDeniedException("You don't have permission to update this course");
        }
        
        try {
            updateCourseFields(course, courseDTO);
            Course updatedCourse = courseRepository.save(course);
            log.debug("Course updated successfully: {}", updatedCourse);
            return convertToDTO(updatedCourse);
        } catch (Exception e) {
            log.error("Error updating course: {}", e.getMessage(), e);
            throw e;
        }
    }

    private boolean isUserAuthorizedToUpdateCourse(Course course, String userEmail) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Admin can update any course
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }
        
        // Users can only update their own courses
        return course.getUser() != null && 
               course.getUser().getEmail().equals(userEmail);
    }

    /**
     * Delete a course.
     *
     * @param id Course ID
     * @throws CourseNotFoundException if course not found
     */
    @Transactional
    public void deleteCourse(Long id) {
        log.info("Deleting course with ID: {}", id);
        
        if (!courseRepository.existsById(id)) {
            log.error("Course not found with ID: {}", id);
            throw new CourseNotFoundException("Course not found with ID: " + id);
        }
        
        courseRepository.deleteById(id);
        log.info("Deleted course with ID: {}", id);
    }

    /**
     * Calculate GPA for given courses.
     *
     * @param courseIds List of course IDs
     * @return Calculated GPA
     */
    public Double calculateGPA(List<Long> courseIds) {
        log.info("Calculating GPA for courses: {}", courseIds);
        return courseRepository.calculateGPA(courseIds);
    }

    // Helper methods for DTO conversion
    private CourseDTO convertToDTO(Course course) {
        return CourseDTO.builder()
            .id(course.getId())
            .courseCode(course.getCourseCode())
            .courseName(course.getCourseName())
            .creditHours(course.getCreditHours())
            .grade(course.getGrade())
            .department(course.getDepartment())
            .materialCount(course.getMaterials() != null ? course.getMaterials().size() : 0)
            .createdAt(course.getCreatedAt())
            .updatedAt(course.getUpdatedAt())
            .build();
    }

    

    private void updateCourseFields(Course course, CourseDTO courseDTO) {
        course.setCourseName(courseDTO.getCourseName());
        course.setCreditHours(courseDTO.getCreditHours());
        course.setGrade(courseDTO.getGrade());
        course.setDepartment(courseDTO.getDepartment());
    }

    private void validateCourse(CourseDTO courseDTO) {
        if (courseDTO.getDepartment() == null) {
            throw new InvalidCourseDataException("Department is required");
        }

        // Validate course code matches department
        String courseCode = courseDTO.getCourseCode();
        Department department = courseDTO.getDepartment();
        if (!courseCode.startsWith(department.getCode())) {
            throw new InvalidCourseDataException(
                String.format("Course code must start with %s for department %s", 
                            department.getCode(), department.name())
            );
        }
    }
}