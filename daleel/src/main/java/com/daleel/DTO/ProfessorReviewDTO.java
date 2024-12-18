package com.daleel.DTO;

import lombok.Data;

/**
 * Data Transfer Object for ProfessorReview entity.
 * 
 * This class is used to transfer review data between the client and server,
 * providing a simplified view of the ProfessorReview entity.
 * 
 * Fields:
 * - id: Unique identifier for the review
 * - professorName: Name of the professor being reviewed
 * - rating: Rating given to the professor
 * - reviewText: Text of the review
 * 
 * @see ProfessorReview
 */
@Data
public class ProfessorReviewDTO {

    /**
     * Unique identifier for the review.
     */
    private Long id;

    /**
     * Name of the professor being reviewed.
     */
    private String professorName;

    /**
     * Rating given to the professor.
     */
    private int rating;

    /**
     * Text of the review.
     */
    private String reviewText;
}