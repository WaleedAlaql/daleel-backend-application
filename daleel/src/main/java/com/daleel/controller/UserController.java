package com.daleel.controller;

import com.daleel.DTO.UserDTO;
import com.daleel.DTO.auth.AuthResponse;
import com.daleel.DTO.auth.LoginRequest;
import com.daleel.DTO.auth.RegisterRequest;
import com.daleel.exception.TokenException;
import com.daleel.exception.UserNotFoundException;
import com.daleel.service.UserService;
import com.daleel.service.auth.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing users.
 * 
 * This controller handles:
 * - User registration with UOH email
 * - User authentication and token generation
 * - User profile management
 * - Account operations (update, delete)
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management and authentication APIs")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    /**
     * Register a new user with UOH credentials.
     * 
     * @param request Registration data including UOH email
     * @return UserDTO with created user information
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user with UOH email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User successfully registered"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authenticationService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticate user and generate JWT token.
     * 
     * @param request Login credentials
     * @return JWT token for authenticated user
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Login with UOH credentials and receive JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "423", description = "Account locked")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }
 
    /**
     * Get user profile by ID.
     * Requires authentication.
     * 
     * @param id User ID to retrieve
     * @return User profile information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user profile", description = "Retrieve user information by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            UserDTO userDTO = userService.getUserDTOById(id);
            return ResponseEntity.ok(userDTO);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    /**
     * Update user profile information.
     * Requires authentication.
     * 
     * @param id User ID to update
     * @param userDTO Updated user information
     * @return Updated user profile
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Update user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user account.
     * Users can only delete their own accounts.
     * 
     * @param id User ID to delete
     * @param token JWT token for authentication
     * @return No content on success
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id)")
    @Operation(summary = "Delete a user", description = "Delete a user by their ID. Requires authentication.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User successfully deleted"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Unauthorized to delete this user")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user profile by JWT token.
     * Requires authentication.
     * 
     * @param token JWT token for authenticated user
     * @return User profile information
     */
    @GetMapping("/profile")
    @Operation(summary = "Get user profile")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    public ResponseEntity<UserDTO> getProfile(@RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix and trim
            String jwtToken = token.substring(7).trim();
            return ResponseEntity.ok(authenticationService.validateTokenAndGetUser(jwtToken));
        } catch (Exception e) {
            throw new TokenException("Invalid token format");
        }
    }
}