package com.daleel.exception;

/**
 * Exception thrown when a course is not found in the system.
 * 
 * This exception is used in scenarios where a course lookup fails,
 * such as when retrieving a course by ID.
 * 
 * @see RuntimeException
 */
public class CourseNotFoundException extends RuntimeException {

    /**
     * Constructs a new CourseNotFoundException with the specified detail message.
     * 
     * @param message The detail message
     */
    public CourseNotFoundException(String message) {
        super(message);
    }
}