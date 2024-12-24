package com.daleel.exception;

import java.net.MalformedURLException;

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
     * @param ex 
     */
    public MaterialNotFoundException(String message, MalformedURLException ex) {
        super(message);
    }

    public MaterialNotFoundException(String message) {
        super(message);
    }
}