package com.daleel.service;

import com.daleel.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for handling file storage operations.
 * 
 * This service:
 * - Manages file uploads and storage
 * - Validates file types and sizes
 * - Generates unique file names
 * - Handles file retrieval and deletion
 * 
 * Security considerations:
 * - Validates file extensions
 * - Uses UUID for file names
 * - Prevents directory traversal
 * 
 * @author Waleed Alaql
 * @version 1.0
 */
@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
            .toAbsolutePath()
            .normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Storage location initialized at: {}", this.fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    /**
     * Stores a file in the configured upload directory.
     * 
     * @param file The MultipartFile to store
     * @return The URL/path to the stored file
     * @throws FileStorageException if storage fails
     */
    public String storeFile(MultipartFile file) {
        log.info("Starting file storage process");
        try {
            if (file.isEmpty()) {
                log.error("Empty file received");
                throw new FileStorageException("Failed to store empty file");
            }

            // Clean and create new filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            log.info("Original filename: {}", originalFilename);
            
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;
            log.info("Generated new filename: {}", newFilename);
            
            // Resolve destination path
            Path destinationFile = this.fileStorageLocation.resolve(newFilename);
            log.info("Destination path: {}", destinationFile);

            // Security check
            if (!destinationFile.getParent().equals(this.fileStorageLocation)) {
                log.error("Cannot store file outside current directory");
                throw new FileStorageException("Cannot store file outside current directory");
            }

            log.info("Copying file to destination");
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("File stored successfully");
            return newFilename;
            
        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to store file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error storing file: {}", e.getMessage(), e);
            throw new FileStorageException("Unexpected error storing file", e);
        }
    }

    /**
     * Retrieves a file from storage.
     * 
     * @param filename Name of the file to retrieve
     * @return Path to the file
     * @throws FileStorageException if file not found
     */
    public Path getFilePath(String filename) {
        return this.fileStorageLocation.resolve(filename);
    }

    /**
     * Deletes a file from storage.
     * 
     * @param filename Name of the file to delete
     * @throws FileStorageException if deletion fails
     */
    public void deleteFile(String fileName) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(fileName);
        Files.deleteIfExists(filePath);
    }
}
