package com.daleel.exception;

/**
 * Exception thrown when an attempt is made to register a user with an email
 * that is already in use.
 * 
 * This exception is used in scenarios where email uniqueness is enforced,
 * such as during user registration or email updates.
 * 
 * @see RuntimeException
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new EmailAlreadyExistsException with the specified detail message.
     * 
     * @param message The detail message
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}