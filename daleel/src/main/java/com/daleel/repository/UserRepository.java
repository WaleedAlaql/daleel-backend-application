package com.daleel.repository;

import com.daleel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Extends JpaRepository to inherit basic CRUD operations:
 * - save(User entity)
 * - findById(Long id)
 * - findAll()
 * - delete(User entity)
 * - deleteById(Long id)
 * etc.
 */
@Repository  // Marks this as a Spring Repository component
public interface UserRepository extends JpaRepository<User, Long> {  // <Entity Type, ID Type>
    
    /**
     * Finds a user by their email address.
     * Used primarily for authentication and user lookup.
     * 
     * @param email The email address to search for
     * @return Optional<User> containing the user if found, empty if not found
     * Usage example: userRepository.findByEmail("student@uoh.edu.sa")
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Checks if a user exists with the given email address.
     * Used during registration to prevent duplicate accounts.
     * 
     * @param email The email address to check
     * @return boolean true if email exists, false otherwise
     * Usage example: userRepository.existsByEmail("student@uoh.edu.sa")
     */
    boolean existsByEmail(String email);
}