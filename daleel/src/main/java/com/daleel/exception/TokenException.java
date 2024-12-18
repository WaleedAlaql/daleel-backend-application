package com.daleel.exception;

/**
 * Exception thrown for JWT token-related errors.
 * Used to provide specific error messages for different token issues.
 */
public class TokenException extends RuntimeException {
    public TokenException(String message) {
        super(message);
    }
}
