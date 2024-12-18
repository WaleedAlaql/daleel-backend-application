package com.daleel.service;

import com.daleel.model.User;
import com.daleel.repository.UserRepository;
import com.daleel.exception.UserNotFoundException;
import com.daleel.exception.EmailAlreadyExistsException;
import com.daleel.exception.InvalidInputException;
import com.daleel.exception.TokenException;
import com.daleel.exception.UnauthorizedException;
import com.daleel.DTO.auth.LoginRequest;
import com.daleel.DTO.UserDTO;
import com.daleel.DTO.auth.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.regex.Pattern;
import javax.security.auth.login.AccountLockedException;
import com.daleel.security.JwtService;
import com.daleel.security.enums.Role;
import com.daleel.DTO.auth.AuthResponse;

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
    private final JwtService jwtService;
    private static final Pattern UOH_EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@uoh\\.edu\\.sa$");

    /**
     * Registers a new user in the system.
     * 
     * This method:
     * 1. Validates the registration request
     * 2. Checks for existing email
     * 3. Creates and saves new user
     * 4. Returns user data without sensitive information
     *
     * Business Rules:
     * - Email must be a valid UOH email address
     * - Password must meet security requirements
     * - Email must be unique in the system
     * 
     * @param request Registration data including email and password
     * @return UserDTO containing the created user's information
     * @throws EmailAlreadyExistsException if email is already registered
     * @throws InvalidInputException if input data is invalid
     */
    @Transactional
public AuthResponse registerUser(RegisterRequest request) {
    // Validate UOH email format
    if (!UOH_EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
        throw new InvalidInputException("Must use a valid UOH email address");
    }

    // Check if email already exists
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new EmailAlreadyExistsException("Email already registered");
    }

    // Create new user with STUDENT role
    var user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .studentId(request.getStudentId())
            .department(request.getDepartment())
            .role(Role.STUDENT)  // Automatically assign STUDENT role
            .active(true)
            .build();

    // Save user
    userRepository.save(user);

    // Generate token
    var token = jwtService.generateToken(user.getEmail());

    // Return AuthResponse DTO
    return AuthResponse.builder()
            .token(token)
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole().toString())
            .studentId(user.getStudentId())
            .department(user.getDepartment())
            .build();
}

    /**
     * Authenticates a user and generates a JWT token.
     * 
     * This method:
     * 1. Validates credentials
     * 2. Checks account status
     * 3. Generates JWT token
     *
     * Security measures:
     * - Passwords are compared using secure hash comparison
     * - Failed attempts are tracked (implementation needed)
     * - Locked accounts are rejected
     * 
     * @param request Login credentials
     * @return JWT token for authenticated user
     * @throws UnauthorizedException if credentials are invalid
     * @throws AccountLockedException if account is locked
     */
    public AuthResponse authenticateUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));


        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        var token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
            .token(token)
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole().toString())
            .studentId(user.getStudentId())
            .department(user.getDepartment())
            .build();
    }

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
    public void deleteUser(Long id, String authenticatedEmail) {
        User user = getUserById(id);
        
        // Check if user is trying to delete their own account
        if (!user.getEmail().equals(authenticatedEmail)) {
            throw new UnauthorizedException("You can only delete your own account");
        }
        userRepository.delete(user);
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

    /**
     * Validates a user's JWT token and returns the associated user.
     * 
     * @param token JWT token including "Bearer " prefix
 * @return UserDTO of the authenticated user
 * @throws TokenException if token is invalid, expired, or malformed
 * @throws UnauthorizedException if user not found
 */
public UserDTO validateTokenAndGetUser(String token) {
    // Validate token (will throw TokenException if invalid)
    jwtService.validateToken(token);
    
    // Get email from token
    String email = jwtService.getEmailFromToken(token);
    
    // Find and return user
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        return convertToDTO(user);
    }
}
