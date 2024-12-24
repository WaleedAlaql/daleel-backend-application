package com.daleel.controller;

// Daleel imports
import com.daleel.DTO.MaterialDTO;
import com.daleel.model.User;
import com.daleel.service.MaterialService;
import com.daleel.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
// Spring imports
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

// Swagger/OpenAPI imports
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;

// Validation

// Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Jackson
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * REST controller for managing educational materials.
 * 
 * This controller handles:
 * - Material uploads and downloads
 * - Material metadata management
 * - Course-specific material listings
 * - Download tracking
 * 
 * Security:
 * - Upload requires authentication
 * - Delete requires ownership
 * - Download and list are public
 */
@RestController
@RequestMapping("/materials")
@RequiredArgsConstructor
@Slf4j
public class MaterialController {

    private final MaterialService materialService;
    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MaterialDTO> uploadMaterial(
            @RequestPart(value = "metadata", required = true) String metadataJson,
            @RequestPart(value = "file", required = true) MultipartFile file,
            Authentication authentication) {
        
        log.info("=== Starting material upload process ===");
        log.info("Received metadata: {}", metadataJson);
        log.info("Received file: {}", file.getOriginalFilename());
        
        try {
            // Parse metadata JSON
            ObjectMapper mapper = new ObjectMapper();
            MaterialDTO materialDTO = mapper.readValue(metadataJson, MaterialDTO.class);
            
            // Get user by email instead of casting
            String userEmail = authentication.getName();
            User user = userService.getUserByEmail(userEmail);
            
            log.info("Processing upload for user: {} (ID: {})", userEmail, user.getId());
            
            MaterialDTO created = materialService.createMaterial(materialDTO, file, user.getId());
            log.info("Material created successfully with ID: {}", created.getId());
            
            return ResponseEntity
                .created(URI.create("/materials/" + created.getId()))
                .body(created);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse metadata JSON", e);
            throw new IllegalArgumentException("Invalid metadata format: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during upload", e);
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Download a material.
     * 
     * @param id Material ID
     * @return Material file
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "Download material", description = "Download material file by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
        @ApiResponse(responseCode = "404", description = "Material not found")
    })
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id) {
        MaterialDTO materialDTO = materialService.getMaterialById(id);
        Resource resource = materialService.loadFileAsResource(materialDTO.getFileUrl());
        
        materialService.incrementDownloads(id);
        
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

    /**
     * Get materials by course code.
     * 
     * @param courseCode Course code to search for
     * @return List of materials
     */
    @GetMapping("/course/{courseCode}")
    @Operation(summary = "List course materials", description = "Get all materials for a specific course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Materials retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid course code")
    })
    public ResponseEntity<List<MaterialDTO>> getMaterialsByCourse(
            @Parameter(description = "Course code (e.g., MATH101)")
            @PathVariable String courseCode) {
        
        List<MaterialDTO> materials = materialService.getMaterialsByCourse(courseCode);
        return ResponseEntity.ok(materials);
    }

    /**
     * Delete a material.
     * 
     * @param id Material ID
     * @param authentication Authentication object
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete material", description = "Delete a material (only by owner)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Material deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not owner"),
        @ApiResponse(responseCode = "404", description = "Material not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteMaterial(
            @PathVariable Long id,
            Authentication authentication) throws IOException {
        log.info("Delete request received for material: {} by user: {}", id, authentication.getName());
        User user = userService.getUserByEmail(authentication.getName());
        materialService.deleteMaterial(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all materials
     */
    @GetMapping
    @Operation(summary = "Get all materials", description = "Retrieve all materials")
    public ResponseEntity<List<MaterialDTO>> getAllMaterials() {
        return ResponseEntity.ok(materialService.getAllMaterials());
    }

    /**
     * Update material
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update material", description = "Update an existing material's metadata")
    public ResponseEntity<MaterialDTO> updateMaterial(
            @PathVariable Long id,
            @RequestBody MaterialDTO materialDTO,
            Authentication authentication) {
        
        User user = userService.getUserByEmail(authentication.getName());
        MaterialDTO updated = materialService.updateMaterial(id, materialDTO, user.getId());
        return ResponseEntity.ok(updated);
    }

    /**
     * Get material by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get material by ID", description = "Retrieve a material by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Material found"),
        @ApiResponse(responseCode = "404", description = "Material not found")
    })
    public ResponseEntity<MaterialDTO> getMaterialById(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.getMaterialById(id));
    }

    /**
     * Get materials uploaded by the authenticated user
     */
    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user materials", description = "Get all materials uploaded by the authenticated user")
    public ResponseEntity<List<MaterialDTO>> getUserMaterials(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        List<MaterialDTO> materials = materialService.getMaterialsByUserId(user.getId());
        return ResponseEntity.ok(materials);
    }
}