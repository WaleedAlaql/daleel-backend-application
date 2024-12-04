package com.daleel.exception;

/**
 * Exception thrown when input validation fails.
 * 
 * This exception is used in scenarios where user input does not meet
 * the required validation criteria, such as invalid email format or
 * password strength.
 * 
 * @see RuntimeException
 */
public class InvalidInputException extends RuntimeException {

    /**
     * Constructs a new InvalidInputException with the specified detail message.
     * 
     * @param message The detail message
     */
    public InvalidInputException(String message) {
        super(message);
    }
}