package com.daleel.exception;

/**
 * Exception thrown when a review is already exists in the system.
 * 
 * This exception is used in scenarios where a review is already exists.
 * 
 * @see RuntimeException
 */
public class DuplicateReviewException extends RuntimeException {

    /**
     * Constructs a new DuplicateReviewException with the specified detail message.
     * 
     * @param message The detail message
     */
    public DuplicateReviewException(String message) {
        super(message);
    }
}