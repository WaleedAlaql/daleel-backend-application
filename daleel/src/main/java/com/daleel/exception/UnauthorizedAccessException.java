package com.daleel.exception;

/**
 * Exception thrown when a user attempts to perform an unauthorized action.
 * 
 * This exception is used in scenarios where access control is violated,
 * such as when a user tries to delete another user's data.
 * 
 * @see RuntimeException
 */
public class UnauthorizedAccessException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedAccessException with the specified detail message.
     * 
     * @param message The detail message
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}