package com.daleel.service;

import com.daleel.model.ProfessorReview;
import com.daleel.repository.ReviewRepository;
import com.daleel.exception.ReviewNotFoundException;
import com.daleel.exception.UnauthorizedAccessException;
import com.daleel.exception.UserNotFoundException;
import com.daleel.exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class responsible for managing professor reviews and ratings.
 * 
 * This service handles:
 * - Creating and managing professor reviews
 * - Retrieving review information
 * - Managing review permissions
 * - Enforcing business rules for reviews
 *
 * Business Rules:
 * - Users must be authenticated to create reviews
 * - Rating must be between 1 and 5
 * - Review text cannot be empty
 * - Users can only delete their own reviews
 * - Admins can delete any review
 *
 * @author Waleed Alaql
 * @version 1.0
 * @see ProfessorReview
 * @see ReviewRepository
 */
@Service
@RequiredArgsConstructor  // Generates constructor for final fields
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;

    /**
     * Creates a new professor review in the system.
     * 
     * This method:
     * 1. Associates the review with the user who created it
     * 2. Validates the review data
     * 3. Saves the review to the database
     * 
     * Validation Rules:
     * - Professor name must not be empty
     * - Rating must be between 1 and 5
     * - Review text must not be empty
     * 
     * @param review The review entity to be created
     * @param userId The ID of the user creating the review
     * @return The created review with generated ID
     * @throws IllegalArgumentException if review data is invalid
     * @throws UserNotFoundException if the user doesn't exist
     * 
     * Usage example:
     * {@code
     * ProfessorReview newReview = new ProfessorReview();
     * newReview.setProfessorName("Dr. Smith");
     * newReview.setRating(5);
     * reviewService.createReview(newReview, currentUserId);
     * }
     */
    @Transactional
    public ProfessorReview createReview(ProfessorReview review, Long userId) {
        review.setUser(userService.getUserById(userId));
        validateReview(review);
        return reviewRepository.save(review);
    }

    /**
     * Retrieves a specific review by its ID.
     * 
     * @param id The unique identifier of the review
     * @return The found review entity
     * @throws ReviewNotFoundException if no review exists with the given ID
     * 
     * Usage example:
     * {@code
     * ProfessorReview review = reviewService.getReviewById(123L);
     * System.out.println("Rating: " + review.getRating());
     * }
     */
    public ProfessorReview getReviewById(Long id) {
        return reviewRepository.findById(id)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));
    }

    /**
     * Retrieves all reviews for a specific professor.
     * 
     * This method returns reviews:
     * - Sorted by default repository order
     * - Including all review details
     * - For the exact professor name match
     * 
     * @param professorName The exact name of the professor
     * @return List of reviews for the professor, empty list if none found
     * @throws IllegalArgumentException if professorName is null or empty
     * 
     * Usage example:
     * {@code
     * List<ProfessorReview> reviews = reviewService.getReviewsByProfessor("Dr. Smith");
     * reviews.forEach(review -> System.out.println(review.getRating()));
     * }
     */
    public List<ProfessorReview> getReviewsByProfessor(String professorName) {
        if (professorName == null || professorName.trim().isEmpty()) {
            throw new IllegalArgumentException("Professor name cannot be empty");
        }
        return reviewRepository.findByProfessorName(professorName);
    }

    /**
     * Deletes a review from the system.
     * 
     * Security Rules:
     * - Only the review creator can delete their review
     * - Admins can delete any review
     * 
     * This operation:
     * 1. Verifies the review exists
     * 2. Checks user permissions
     * 3. Permanently deletes the review
     * 
     * @param id The ID of the review to delete
     * @param userId The ID of the user attempting to delete
     * @param isAdmin Whether the user has admin privileges
     * @throws ReviewNotFoundException if review doesn't exist
     * @throws UnauthorizedAccessException if user is not authorized
     * 
     * Usage example:
     * {@code
     * try {
     *     reviewService.deleteReview(reviewId, currentUserId, isAdmin);
     *     System.out.println("Review deleted successfully");
     * } catch (UnauthorizedAccessException e) {
     *     System.out.println("Not authorized to delete this review");
     * }
     * }
     */
    @Transactional
    public void deleteReview(Long id, Long userId, boolean isAdmin) {
        ProfessorReview review = getReviewById(id);
        
        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only delete your own reviews");
        }
        
        reviewRepository.delete(review);
    }

    /**
     * Validates a review before saving.
     * 
     * Validation rules:
     * - Professor name must not be empty
     * - Rating must be between 1 and 5
     * - Review text must not be empty
     * 
     * @param review The review to validate
     * @throws InvalidInputException if validation fails
     */
    private void validateReview(ProfessorReview review) {
        if (review.getProfessorName() == null || review.getProfessorName().trim().isEmpty()) {
            throw new InvalidInputException("Professor name is required");
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new InvalidInputException("Rating must be between 1 and 5");
        }
        if (review.getReviewText() == null || review.getReviewText().trim().isEmpty()) {
            throw new InvalidInputException("Review text cannot be empty");
        }
    }
}