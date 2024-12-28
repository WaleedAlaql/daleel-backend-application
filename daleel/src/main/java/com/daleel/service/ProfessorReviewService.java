package com.daleel.service;

import com.daleel.model.ProfessorReview;
import com.daleel.model.User;
import com.daleel.repository.ReviewRepository;
import com.daleel.repository.UserRepository;
import com.daleel.exception.ReviewNotFoundException;
import com.daleel.exception.UnauthorizedAccessException;
import com.daleel.exception.UserNotFoundException;
import com.daleel.DTO.ProfessorReviewDTO;
import com.daleel.exception.DuplicateReviewException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

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
@RequiredArgsConstructor
@Slf4j
public class ProfessorReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    /**
     * Create a new professor review
     */
    @Transactional
    public ProfessorReviewDTO createReview(ProfessorReviewDTO reviewDTO) {
        log.debug("Creating review for professor: {}", reviewDTO.getProfessorName());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        // Check if user already reviewed this course
        if (reviewRepository.existsByUserEmailAndCourseCode(currentUserEmail, reviewDTO.getCourseCode())) {
            throw new DuplicateReviewException("You have already reviewed this course");
        }

        User user = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        ProfessorReview review = ProfessorReview.builder()
            .professorName(reviewDTO.getProfessorName())
            .courseCode(reviewDTO.getCourseCode())
            .rating(reviewDTO.getRating())
            .reviewText(reviewDTO.getReviewText())
            .user(user)
            .build();

        ProfessorReview savedReview = reviewRepository.save(review);
        return convertToDTO(savedReview);
    }

    /**
     * Update an existing review
     */
    @Transactional
    public ProfessorReviewDTO updateReview(Long id, ProfessorReviewDTO reviewDTO) {
        log.debug("Updating review: {}", id);

        ProfessorReview review = reviewRepository.findById(id)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        // Check if user owns the review
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!review.getUser().getEmail().equals(auth.getName())) {
            throw new UnauthorizedAccessException("You can only update your own reviews");
        }

        review.setProfessorName(reviewDTO.getProfessorName());
        review.setRating(reviewDTO.getRating());
        review.setReviewText(reviewDTO.getReviewText());

        ProfessorReview updatedReview = reviewRepository.save(review);
        return convertToDTO(updatedReview);
    }

    /**
     * Get reviews by course code
     */
    public List<ProfessorReviewDTO> getReviewsByCourse(String courseCode) {
        return reviewRepository.findByCourseCode(courseCode).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get reviews by professor name
     */
    public List<ProfessorReviewDTO> getReviewsByProfessor(String professorName) {
        return reviewRepository.findByProfessorNameContainingIgnoreCase(professorName).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get average rating for professor
     */
    public Double getProfessorAverageRating(String professorName) {
        return reviewRepository.getAverageRatingForProfessor(professorName);
    }

    /**
     * Delete a review
     */
    @Transactional
    public void deleteReview(Long id) {
        ProfessorReview review = reviewRepository.findById(id)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!review.getUser().getEmail().equals(auth.getName()) &&
            !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedAccessException("You can only delete your own reviews");
        }

        reviewRepository.deleteById(id);
    }

    private ProfessorReviewDTO convertToDTO(ProfessorReview review) {
        return ProfessorReviewDTO.builder()
            .id(review.getId())
            .professorName(review.getProfessorName())
            .courseCode(review.getCourseCode())
            .rating(review.getRating())
            .reviewText(review.getReviewText())
            .userName(review.getUser().getName())
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .build();
    }
}