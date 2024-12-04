package com.daleel.exception;

/**
 * Exception thrown when a material is not found in the system.
 * 
 * This exception is used in scenarios where a material lookup fails,
 * such as when retrieving a material by ID.
 * 
 * @see RuntimeException
 */
public class MaterialNotFoundException extends RuntimeException {

    /**
     * Constructs a new MaterialNotFoundException with the specified detail message.
     * 
     * @param message The detail message
     */
    public MaterialNotFoundException(String message) {
        super(message);
    }
}