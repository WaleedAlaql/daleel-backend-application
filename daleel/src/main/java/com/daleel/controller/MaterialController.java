package com.daleel.controller;

import com.daleel.model.Material;
import com.daleel.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for managing educational materials.
 * 
 * This controller provides endpoints for:
 * - Material upload and download
 * - Material management
 * - Material search
 */
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Tag(name = "Material", description = "Educational material management APIs")
public class MaterialController {

    private final MaterialService materialService;

    /**
     * Creates a new material.
     * 
     * @param material The material data
     * @param userId The ID of the user creating the material
     * @return The created material
     */
    @PostMapping
    @Operation(
        summary = "Upload new material",
        description = "Uploads a new educational material"
    )
    @ApiResponse(responseCode = "201", description = "Material created successfully")
    public ResponseEntity<Material> createMaterial(
            @Valid @RequestBody Material material,
            @RequestParam Long userId  // Added userId parameter
    ) {
        Material createdMaterial = materialService.createMaterial(material, userId);
        return ResponseEntity.ok(createdMaterial);
    }

    /**
     * Retrieves a material by ID.
     * 
     * @param id The material ID
     * @return The material information
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get material by ID",
        description = "Retrieves material information based on the provided ID"
    )
    public ResponseEntity<Material> getMaterialById(@PathVariable Long id) {
        Material material = materialService.getMaterialById(id);
        return ResponseEntity.ok(material);
    }

    /**
     * Retrieves materials by course code.
     * 
     * @param courseCode The course code
     * @return List of materials
     */
    @GetMapping("/course/{courseCode}")
    @Operation(
        summary = "Get materials by course",
        description = "Retrieves all materials for a specific course"
    )
    public ResponseEntity<List<Material>> getMaterialsByCourse(@PathVariable String courseCode) {
        List<Material> materials = materialService.getMaterialsByCourse(courseCode);
        return ResponseEntity.ok(materials);
    }

    /**
     * Updates material information.
     * 
     * @param id The material ID
     * @param material The updated material data
     * @return The updated material
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update material",
        description = "Updates material information"
    )
    public ResponseEntity<Material> updateMaterial(
            @PathVariable Long id,
            @Valid @RequestBody Material material
    ) {
        Material updatedMaterial = materialService.updateMaterial(id, material);
        return ResponseEntity.ok(updatedMaterial);
    }

    /**
     * Deletes a material.
     * 
     * @param id The material ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete material",
        description = "Deletes material based on the provided ID"
    )
    public ResponseEntity<Void> deleteMaterial(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam boolean isAdmin  
    ) {
        materialService.deleteMaterial(id, userId, isAdmin);
        return ResponseEntity.noContent().build();
    }
}