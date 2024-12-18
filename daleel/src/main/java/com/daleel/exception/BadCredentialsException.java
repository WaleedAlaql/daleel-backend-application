package com.daleel.exception;


/**
 * Exception thrown when bad credentials are provided.
 * 
 * This exception is used in scenarios where a user provides invalid credentials,
 * such as when logging in.
 * 
 * @see RuntimeException
 */
public class BadCredentialsException extends RuntimeException {

    /**
     * Constructs a new BadCredentialsException with the specified detail message.
     * 
     * @param message The detail message
     */
    public BadCredentialsException(String message) {
        super(message);
    }
}