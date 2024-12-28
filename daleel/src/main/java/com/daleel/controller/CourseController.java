package com.daleel.controller;

import com.daleel.DTO.CourseDTO;
import com.daleel.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

/**
 * REST controller for managing courses.
 */
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Course Management", description = "APIs for managing courses")
public class CourseController {

    private final CourseService courseService;

    /**
     * Create a new course.
          * @throws Exception 
          */
         @PostMapping
         @PreAuthorize("isAuthenticated()")
         @Operation(summary = "Create a new course")
         @ApiResponses(value = {
             @ApiResponse(responseCode = "201", description = "Course created successfully"),
             @ApiResponse(responseCode = "400", description = "Invalid input"),
             @ApiResponse(responseCode = "409", description = "Course code already exists")
         })
         public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) throws Exception {
        log.debug("REST request to create Course : {}", courseDTO);
        try {
            CourseDTO result = courseService.createCourse(courseDTO);
            log.info("Course created successfully with ID: {}", result.getId());
            return ResponseEntity.created(new URI("/api/courses/" + result.getId()))
                .body(result);
        } catch (Exception e) {
            log.error("Failed to create course", e);
            throw e;
        }
    }

    /**
     * Get all courses.
     */
    @GetMapping
    @Operation(summary = "Get all courses")
    @ApiResponse(responseCode = "200", description = "List of courses retrieved successfully")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        log.info("REST request to get all Courses");
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    /**
     * Get course by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Course found"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        log.info("REST request to get Course : {}", id);
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    /**
     * Get course by code.
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "Get course by code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Course found"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<CourseDTO> getCourseByCode(@PathVariable String code) {
        log.info("REST request to get Course by code : {}", code);
        return ResponseEntity.ok(courseService.getCourseByCode(code));
    }

    /**
     * Get courses by department.
     */
    @GetMapping("/department/{department}")
    @Operation(summary = "Get courses by department")
    @ApiResponse(responseCode = "200", description = "List of courses retrieved successfully")
    public ResponseEntity<List<CourseDTO>> getCoursesByDepartment(@PathVariable String department) {
        log.info("REST request to get Courses by department : {}", department);
        return ResponseEntity.ok(courseService.getCoursesByDepartment(department));
    }

    /**
     * Update course.
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Course updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDTO courseDTO) {
        log.debug("REST request to update Course : {}, {}", id, courseDTO);
        try {
            CourseDTO result = courseService.updateCourse(id, courseDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error updating course: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete course.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Course deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        log.info("REST request to delete Course : {}", id);
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Calculate GPA.
     */
    @PostMapping("/gpa")
    @Operation(summary = "Calculate GPA")
    @ApiResponse(responseCode = "200", description = "GPA calculated successfully")
    public ResponseEntity<Double> calculateGPA(@RequestBody List<Long> courseIds) {
        log.info("REST request to calculate GPA for courses : {}", courseIds);
        return ResponseEntity.ok(courseService.calculateGPA(courseIds));
    }
}
