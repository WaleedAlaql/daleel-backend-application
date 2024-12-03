package com.daleel.service;

import com.daleel.model.User;
import com.daleel.repository.UserRepository;
import com.daleel.exception.UserNotFoundException;
import com.daleel.exception.EmailAlreadyExistsException;
import com.daleel.exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Email pattern for UOH email addresses
    private static final Pattern UOH_EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9._%+-]+@uoh\\.edu\\.sa$");
    
    // Password requirements pattern
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    /**
     * Creates a new user account in the system.
     * 
     * This method:
     * 1. Validates user input data
     * 2. Checks for existing email
     * 3. Encodes the password
     * 4. Creates the user account
     * 
     * Validation Rules:
     * - Email must be a valid UOH email address
     * - Password must meet security requirements
     * - Name must not be empty
     * - Email must be unique in the system
     * 
     * @param user The user entity to be created
     * @return The created user with generated ID
     * @throws EmailAlreadyExistsException if email is already registered
     * @throws InvalidInputException if validation fails
     * 
     * Usage example:
     * {@code
     * User newUser = new User();
     * newUser.setEmail("student@uoh.edu.sa");
     * newUser.setPassword("SecurePass123!");
     * newUser.setName("John Doe");
     * User createdUser = userService.createUser(newUser);
     * }
     */
    @Transactional
    public User createUser(User user) {
        // Validate input
        validateNewUser(user);

        // Check for existing email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + user.getEmail());
        }

        // Encode password and prepare user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);

        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their ID.
     * 
     * This method is commonly used for:
     * - Profile retrieval
     * - Authorization checks
     * - User data management
     * 
     * @param id The unique identifier of the user
     * @return The found user entity
     * @throws UserNotFoundException if no user exists with the given ID
     * 
     * Usage example:
     * {@code
     * User user = userService.getUserById(123L);
     * System.out.println("User name: " + user.getName());
     * }
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    /**
     * Retrieves a user by their email address.
     * 
     * Commonly used for:
     * - Authentication
     * - Password reset
     * - Profile lookup
     * 
     * @param email The email address to search for
     * @return The found user entity
     * @throws UserNotFoundException if no user exists with the given email
     * 
     * Usage example:
     * {@code
     * User user = userService.getUserByEmail("student@uoh.edu.sa");
     * System.out.println("Found user: " + user.getName());
     * }
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    /**
     * Updates user information.
     * 
     * This method:
     * 1. Validates the update data
     * 2. Updates only provided fields
     * 3. Maintains password security
     * 
     * Security considerations:
     * - Password is re-encoded if updated
     * - Email cannot be changed (business rule)
     * - Only non-null fields are updated
     * 
     * @param id The ID of the user to update
     * @param userDetails The user entity containing updated fields
     * @return The updated user entity
     * @throws UserNotFoundException if user doesn't exist
     * @throws InvalidInputException if validation fails
     * 
     * Usage example:
     * {@code
     * User updates = new User();
     * updates.setName("New Name");
     * User updatedUser = userService.updateUser(userId, updates);
     * }
     */
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User existingUser = getUserById(id);
        
        // Update name if provided
        if (userDetails.getName() != null && !userDetails.getName().trim().isEmpty()) {
            existingUser.setName(userDetails.getName().trim());
        }
        
        // Update password if provided
        if (userDetails.getPassword() != null) {
            validatePassword(userDetails.getPassword());
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        return userRepository.save(existingUser);
    }

    /**
     * Validates a new user's data before creation.
     * 
     * Checks:
     * - Email format (must be UOH email)
     * - Password strength
     * - Name requirements
     * 
     * @param user The user to validate
     * @throws InvalidInputException if validation fails
     */
    private void validateNewUser(User user) {
        // Validate email
        if (user.getEmail() == null || !UOH_EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new InvalidInputException("Invalid email format. Must be a UOH email address");
        }

        // Validate password
        validatePassword(user.getPassword());

        // Validate name
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new InvalidInputException("Name is required");
        }
    }

    /**
     * Validates password strength requirements.
     * 
     * Requirements:
     * - Minimum 8 characters
     * - At least one digit
     * - At least one lowercase letter
     * - At least one uppercase letter
     * - At least one special character
     * - No whitespace
     * 
     * @param password The password to validate
     * @throws InvalidInputException if password is invalid
     */
    private void validatePassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new InvalidInputException(
                "Password must be at least 8 characters long and contain at least: " +
                "one digit, one lowercase letter, one uppercase letter, " +
                "one special character, and no whitespace"
            );
        }
    }
}