package com.daleel.service;

import com.daleel.model.User;
import com.daleel.repository.MaterialRepository;
import com.daleel.repository.UserRepository;
import com.daleel.exception.UserNotFoundException;
import com.daleel.exception.EmailAlreadyExistsException;
import com.daleel.exception.InvalidInputException;
import com.daleel.exception.UnauthorizedException;
import com.daleel.DTO.UserDTO;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.regex.Pattern;

/**
 * Service class responsible for managing user operations and business logic.
 * 
 * This service handles:
 * - User registration and account management
 * - User authentication support
 * - Profile updates and management
 * - Security and validation rules
 *
 * Business Rules:
 * - Users must register with a UOH email address
 * - Passwords must meet security requirements
 * - Email addresses must be unique in the system
 * - User accounts can be active or inactive
 *
 * @author Waleed Alaql
 * @version 1.0
 * @see User
 * @see UserRepository
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;
    private static final Pattern UOH_EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@uoh\\.edu\\.sa$");


        /**
     * Helper method to get user by ID.
     * Internal use only.
     * 
     * @param id The ID of the user to retrieve
     * @return User entity
     * @throws UserNotFoundException if user doesn't exist
     */
    User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    /**
     * Retrieves a user by their ID and converts to DTO
     * 
     * @param id The ID of the user to retrieve
     * @return UserDTO containing user information
     * @throws UserNotFoundException if user doesn't exist
     */
    public UserDTO getUserDTOById(Long id) {
        User user = getUserById(id);  // Reuse the helper method
        
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .studentId(user.getStudentId())
                .department(user.getDepartment())
                .role(user.getRole().toString())
                .build();
    }

    /**
 * Get user by email.
 * Internal use only - used by security filters.
 * 
 * @param email The email to search for
 * @return User entity
 * @throws UserNotFoundException if user doesn't exist
 */
public User getUserByEmail(String email) {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }   

        /**
     * Updates user information.
     * 
     * @param id User ID to update
     * @param userDTO Updated user information
     * @return UserDTO with updated information
     * @throws UserNotFoundException if user doesn't exist
     * @throws EmailAlreadyExistsException if new email is already in use
     * @throws InvalidInputException if update data is invalid
     */
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        // Get existing user
        User user = getUserById(id);
        
        // Check email uniqueness if changed
        if (userDTO.getEmail() != null && !user.getEmail().equals(userDTO.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new EmailAlreadyExistsException("Email already in use: " + userDTO.getEmail());
            }
            if (!UOH_EMAIL_PATTERN.matcher(userDTO.getEmail()).matches()) {
                throw new InvalidInputException("Must use a valid UOH email address");
            }
            user.setEmail(userDTO.getEmail());
        }

        // Update allowed fields (only if provided in userDTO)
        if (userDTO.getName() != null) {
            user.setName(userDTO.getName());
        }
        if (userDTO.getStudentId() != null) {
            user.setStudentId(userDTO.getStudentId());
        }
        if (userDTO.getDepartment() != null) {
            user.setDepartment(userDTO.getDepartment());
        }

        // Save the updated user
        User updatedUser = userRepository.save(user);
        
        // Convert and return
        return convertToDTO(updatedUser);
    }

        /**
     * Deletes a user account.
     * Users can only delete their own accounts.
     * 
     * @param id User ID to delete
     * @param authenticatedEmail Email from JWT token
     * @throws UserNotFoundException if user doesn't exist
     * @throws UnauthorizedException if trying to delete another user's account
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
            
        if (!isCurrentUser(id)) {
            throw new AccessDeniedException("Not authorized to delete this user");
        }
        
        // Delete associated materials first
        materialRepository.deleteByUserId(id);
        
        // Then delete the user
        userRepository.delete(user);
    }

public boolean isCurrentUser(Long userId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
        return false;
    }
    
    // If user is admin, allow access
    if (auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
        return true;
    }
    
    // Check if current user matches requested user ID
    return userRepository.findById(userId)
        .map(user -> user.getEmail().equals(auth.getName()))
        .orElse(false);
}

     @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password(user.getPassword())
            .roles(user.getRole().name())
            .build();
    }

    /**
     * Converts User entity to UserDTO.
     * Removes sensitive information.
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setStudentId(user.getStudentId());
        dto.setDepartment(user.getDepartment());
        dto.setRole(user.getRole().toString());
        // Add other fields as needed
        return dto;
    }
}
