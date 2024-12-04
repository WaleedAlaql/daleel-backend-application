package com.daleel.exception;

/**
 * Exception thrown when a user is not found in the system.
 * 
 * This exception is used in scenarios where a user lookup fails,
 * such as when retrieving a user by ID or email.
 * 
 * @see RuntimeException
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a new UserNotFoundException with the specified detail message.
     * 
     * @param message The detail message
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}