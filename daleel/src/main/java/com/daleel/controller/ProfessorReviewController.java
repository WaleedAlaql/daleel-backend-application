package com.daleel.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.daleel.DTO.ProfessorReviewDTO;
import com.daleel.service.ProfessorReviewService;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Professor Reviews", description = "APIs for managing professor reviews")
public class ProfessorReviewController {

    private final ProfessorReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create new review")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Review created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Review already exists for this course")
    })
    public ResponseEntity<ProfessorReviewDTO> createReview(@Valid @RequestBody ProfessorReviewDTO reviewDTO) {
        log.debug("REST request to create Review : {}", reviewDTO);
        ProfessorReviewDTO result = reviewService.createReview(reviewDTO);
        return ResponseEntity.created(URI.create("/reviews/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update review")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Review updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ProfessorReviewDTO> updateReview(
            @PathVariable Long id, 
            @Valid @RequestBody ProfessorReviewDTO reviewDTO) {
        log.debug("REST request to update Review : {}", id);
        ProfessorReviewDTO result = reviewService.updateReview(id, reviewDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/course/{courseCode}")
    @Operation(summary = "Get reviews by course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid course code")
    })
    public ResponseEntity<List<ProfessorReviewDTO>> getReviewsByCourse(
            @Parameter(description = "Course code (e.g., CS101)")
            @PathVariable String courseCode) {
        log.debug("REST request to get Reviews for course : {}", courseCode);
        List<ProfessorReviewDTO> reviews = reviewService.getReviewsByCourse(courseCode);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/professor/{professorName}")
    @Operation(summary = "Get reviews by professor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully")
    })
    public ResponseEntity<List<ProfessorReviewDTO>> getReviewsByProfessor(
            @Parameter(description = "Professor name")
            @PathVariable String professorName) {
        log.debug("REST request to get Reviews for professor : {}", professorName);
        List<ProfessorReviewDTO> reviews = reviewService.getReviewsByProfessor(professorName);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/professor/{professorName}/rating")
    @Operation(summary = "Get professor's average rating")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Average rating retrieved successfully")
    })
    public ResponseEntity<Double> getProfessorRating(
            @Parameter(description = "Professor name")
            @PathVariable String professorName) {
        log.debug("REST request to get average rating for professor : {}", professorName);
        Double rating = reviewService.getProfessorAverageRating(professorName);
        return ResponseEntity.ok(rating);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete review")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Review not found"),
        @ApiResponse(responseCode = "403", description = "Not authorized to delete this review")
    })
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        log.debug("REST request to delete Review : {}", id);
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
