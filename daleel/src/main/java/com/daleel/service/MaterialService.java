package com.daleel.service;

import com.daleel.model.Material;
import com.daleel.model.User;
import com.daleel.repository.MaterialRepository;

import io.jsonwebtoken.io.IOException;

import com.daleel.exception.MaterialNotFoundException;
import com.daleel.exception.UnauthorizedAccessException;
import com.daleel.exception.FileStorageException;
import com.daleel.exception.InvalidInputException;
import com.daleel.DTO.MaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service responsible for managing educational materials.
 * 
 * This service handles:
 * - Material creation and management
 * - File validation and storage
 * - Download tracking
 * - Access control
 * 
 * Business Rules:
 * - Only authenticated users can upload materials
 * - Users can only modify/delete their own materials
 * - Materials must have valid course codes
 * - File size and type restrictions apply
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    @Value("${file.max-size:10485760}") // 10MB default
    private long maxFileSize;

    @Value("${file.allowed-types:pdf,doc,docx}")
    private String[] allowedTypes;
    
    /**
     * Creates a new material with uploaded file.
     * 
     * @param materialDTO Material metadata
     * @param file Uploaded file
     * @param userId ID of uploading user
     * @return Created material DTO
     */
    @Transactional
    public MaterialDTO createMaterial(MaterialDTO materialDTO, MultipartFile file, Long userId) {
        log.info("Creating material for user: {}", userId);
        
        validateFile(file);
        User user = userService.getUserById(userId);
        
        try {
            String storedFilename = fileStorageService.storeFile(file);
            
            Material material = Material.builder()
            .title(materialDTO.getTitle())
            .description(materialDTO.getDescription())
            .courseCode(materialDTO.getCourseCode())
            .courseName(materialDTO.getCourseName())
            .fileUrl(storedFilename)
            .fileType(getFileExtension(file.getOriginalFilename()))
            .fileSize(file.getSize())
    .user(user)
    .downloads(0)
    .uploadDate(LocalDateTime.now())
    .build();

            Material savedMaterial = materialRepository.save(material);
            return convertToDTO(savedMaterial);

        } catch (Exception e) {
            log.error("Error creating material: ", e);
            throw new RuntimeException("Could not create material", e);
        }
    }

    /**
     * Retrieves a material by ID.
     * 
     * @param id Material ID
     * @return Material DTO
     * @throws MaterialNotFoundException if not found
     */
    public MaterialDTO getMaterialById(Long id) {
        Material material = materialRepository.findById(id)
            .orElseThrow(() -> new MaterialNotFoundException("Material not found with id: " + id));
        return convertToDTO(material);
    }

    /**
     * Lists all materials for a course.
     * 
     * @param courseCode Course code to search for
     * @return List of material DTOs
     */
    public List<MaterialDTO> getMaterialsByCourse(String courseCode) {
        return materialRepository.findByCourseCode(courseCode).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get materials uploaded by a specific user
     */
    public List<MaterialDTO> getMaterialsByUserId(Long userId) {
        return materialRepository.findByUserId(userId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get all materials without pagination
     */
    public List<MaterialDTO> getAllMaterials() {
        return materialRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Update an existing material
     */
    @Transactional
    public MaterialDTO updateMaterial(Long id, MaterialDTO materialDTO, Long userId) {
        Material material = materialRepository.findById(id)
            .orElseThrow(() -> new MaterialNotFoundException("Material not found with id: " + id));
        
        // Check if user owns the material
        if (!material.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only update your own materials");
        }
        
        material.setTitle(materialDTO.getTitle());
        material.setDescription(materialDTO.getDescription());
        material.setCourseCode(materialDTO.getCourseCode());
        material.setCourseName(materialDTO.getCourseName());
        
        Material updatedMaterial = materialRepository.save(material);
        return convertToDTO(updatedMaterial);
    }

    /**
     * Updates material download count.
     * 
     * @param id Material ID
     */
    @Transactional
    public void incrementDownloads(Long id) {
        materialRepository.incrementDownloads(id);
    }

    /**
     * Deletes a material and its associated file.
     * 
     * @param id Material ID
     * @param userId ID of user attempting deletion
     * @throws java.io.IOException 
     * @throws UnauthorizedAccessException if user isn't the owner
     */
    @Transactional
    public void deleteMaterial(Long id, Long userId) {
    log.info("Attempting to delete material with id: {} by user: {}", id, userId);
    
    Material material = materialRepository.findById(id)
        .orElseThrow(() -> {
            log.error("Material not found with id: {}", id);
            return new MaterialNotFoundException("Material not found with id: " + id);
        });
    
    // Check if user owns the material
    if (!material.getUser().getId().equals(userId)) {
        log.error("User {} is not authorized to delete material {}", userId, id);
        throw new UnauthorizedAccessException("You can only delete your own materials");
    }
    
    try {
        // Delete the file first
        log.info("Attempting to delete file: {}", material.getFileUrl());
        fileStorageService.deleteFile(material.getFileUrl());
        log.info("File deleted successfully");
        
        // Delete from database using direct query
        materialRepository.deleteMaterialById(id);
        
        // Flush to ensure the delete is executed immediately
        materialRepository.flush();
        
        log.info("Material {} deleted successfully from database", id);
    } catch (IOException e) {
        log.error("Error deleting material file: {}", e.getMessage(), e);
        throw new FileStorageException("Could not delete material file", e);
    } catch (Exception e) {
        log.error("Error deleting material from database: {}", e.getMessage(), e);
        throw new RuntimeException("Could not delete material from database", e);
    }
}

    /**
    * Loads a file as a Resource for downloading.
    * 
    * @param fileUrl The file URL/name to load
    * @return Resource containing the file
    * @throws MaterialNotFoundException if file doesn't exist
    */
    public Resource loadFileAsResource(String fileUrl) {
        try {
            Path filePath = fileStorageService.getFilePath(fileUrl);
            Resource resource = new UrlResource(filePath.toUri());
            
        if (resource.exists()) {
            return resource;
        } else {
            throw new MaterialNotFoundException("File not found: " + fileUrl);
        }
    } catch (MalformedURLException ex) {
        throw new MaterialNotFoundException("File not found: " + fileUrl, ex);
    }
}

    // Private helper methods...
    private void validateFile(MultipartFile file) {
        log.info("Validating file...");
        
        if (file == null) {
            log.error("File is null");
            throw new InvalidInputException("File is required");
        }

        if (file.isEmpty()) {
            log.error("File is empty");
            throw new InvalidInputException("File cannot be empty");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            log.error("File size {} exceeds maximum limit of {}", 
                file.getSize(), maxFileSize);
            throw new InvalidInputException("File size exceeds maximum limit of 10MB");
        }

        // Get original filename and extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            log.error("Invalid file name");
            throw new InvalidInputException("Invalid file name");
        }

        // Validate file extension
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        log.info("File extension: {}", fileExtension);
        if (!List.of(allowedTypes).contains(fileExtension)) {
            log.error("Invalid file type: {}. Allowed types: {}", 
                fileExtension, Arrays.toString(allowedTypes));
            throw new InvalidInputException("File type not allowed. Allowed types: pdf, doc, docx");
        }

        // Validate content type
        String contentType = file.getContentType();
        log.info("Content type: {}", contentType);
        if (contentType == null || !isValidContentType(contentType)) {
            log.error("Invalid content type: {}", contentType);
            throw new InvalidInputException("Invalid file type");
        }

        log.info("File validation successful");
    }
    
    private boolean isValidContentType(String contentType) {
        return contentType.equals("application/pdf") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
            .filter(f -> f.contains("."))
            .map(f -> f.substring(filename.lastIndexOf(".") + 1))
            .orElse("");
    }

    private MaterialDTO convertToDTO(Material material) {
        return MaterialDTO.builder()
            .id(material.getId())
            .title(material.getTitle())
            .description(material.getDescription())
            .courseCode(material.getCourseCode())
            .courseName(material.getCourseName())
            .uploaderName(material.getUser().getName())
            .downloads(material.getDownloads())
            .uploadDate(material.getUploadDate())
            .fileType(material.getFileType())
            .fileSize(material.getFileSize())
            .fileUrl(material.getFileUrl())
            .build();
    }
}