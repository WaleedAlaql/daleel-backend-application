package com.daleel.exception;

/**
 * Exception thrown when a course is already exists in the system.
 * 
 * This exception is used in scenarios where a course is already exists.
 * 
 * @see RuntimeException
 */
public class DuplicateCourseException extends RuntimeException {

    /**
     * Constructs a new DuplicateCourseException with the specified detail message.
     * 
     * @param message The detail message
     */
    public DuplicateCourseException(String message) {
        super(message);
    }
}