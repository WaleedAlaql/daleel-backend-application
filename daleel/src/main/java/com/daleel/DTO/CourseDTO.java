package com.daleel.DTO;

import lombok.Data;

/**
 * Data Transfer Object for Course entity.
 * 
 * This class is used to transfer course data between the client and server,
 * providing a simplified view of the Course entity.
 * 
 * Fields:
 * - id: Unique identifier for the course
 * - code: Course code (e.g., "MATH101")
 * - name: Name of the course
 * - creditHours: Number of credit hours for the course
 * - grade: Grade received in the course
 * 
 * @see Course
 */
@Data
public class CourseDTO {

    /**
     * Unique identifier for the course.
     */
    private Long id;

    /**
     * Course code (e.g., "MATH101").
     */
    private String code;

    /**
     * Name of the course.
     */
    private String name;

    /**
     * Number of credit hours for the course.
     */
    private int creditHours;

    /**
     * Grade received in the course.
     */
    private String grade;
}