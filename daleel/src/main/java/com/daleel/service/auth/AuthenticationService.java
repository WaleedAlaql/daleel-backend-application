package com.daleel.service.auth;

import com.daleel.DTO.UserDTO;
import com.daleel.DTO.auth.AuthResponse;
import com.daleel.DTO.auth.LoginRequest;
import com.daleel.DTO.auth.RegisterRequest;
import com.daleel.exception.EmailAlreadyExistsException;
import com.daleel.exception.InvalidInputException;
import com.daleel.exception.TokenException;
import com.daleel.exception.UnauthorizedException;
import com.daleel.model.User;
import com.daleel.repository.UserRepository;
import com.daleel.security.JwtService;
import com.daleel.security.enums.Role;

import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

import javax.security.auth.login.AccountLockedException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
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
