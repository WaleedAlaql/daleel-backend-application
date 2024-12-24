package com.daleel.exception;

/**
 * Exception thrown when there is an error in file storage operations.
 * 
 * This exception is used to handle errors related to file uploads, downloads, and deletions.
 * 
 * @see RuntimeException
 */
public class FileStorageException extends RuntimeException {
    
    /**
     * Constructs a new FileStorageException with the specified detail message.
     * 
     * @param message The detail message
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileStorageException with the specified detail message and cause.
     * 
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
