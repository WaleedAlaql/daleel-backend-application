package com.daleel.exception;

/**
 * Exception thrown when a review is not found in the system.
 * 
 * This exception is used in scenarios where a review lookup fails,
 * such as when retrieving a review by ID.
 * 
 * @see RuntimeException
 */
public class ReviewNotFoundException extends RuntimeException {

    /**
     * Constructs a new ReviewNotFoundException with the specified detail message.
     * 
     * @param message The detail message
     */
    public ReviewNotFoundException(String message) {
        super(message);
    }
}