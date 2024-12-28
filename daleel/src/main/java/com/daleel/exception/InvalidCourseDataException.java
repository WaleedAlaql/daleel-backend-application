package com.daleel.exception;

/**
 * Exception thrown when a course data is not valid.
 * 
 * This exception is used in scenarios where a course data is not valid,
 * such as when a course code does not match the department code.
 * 
 * @see RuntimeException
 */
public class InvalidCourseDataException extends RuntimeException {

    /**
     * Constructs a new InvalidCourseDataException with the specified detail message.
     * 
     * @param message The detail message
     */
    public InvalidCourseDataException(String message) {
        super(message);
    }
}