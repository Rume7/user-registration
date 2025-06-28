package com.codehacks.user_registration.controller;

import com.codehacks.user_registration.dto.UserRegistrationRequest;
import com.codehacks.user_registration.dto.UserResponse;
import com.codehacks.user_registration.model.User;
import com.codehacks.user_registration.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * UserController - REST API Controller for User operations
 *
 * This class demonstrates:
 * 1. RESTful API design principles
 * 2. OpenAPI/Swagger documentation annotations
 * 3. Input validation with @Valid
 * 4. Proper HTTP status codes and response handling
 * 5. Error handling and exception management
 * 6. Integration between API layer and service layer
 *
 * REST Controller Responsibilities:
 * - Handle HTTP requests and responses
 * - Validate input data
 * - Convert between DTOs and domain objects
 * - Return appropriate HTTP status codes
 * - Document API endpoints for client consumption
 *
 * OpenAPI/Swagger Benefits:
 * - Automatic API documentation generation
 * - Interactive API testing interface
 * - Client code generation capabilities
 * - API contract validation and consistency
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user registration and management")
public class UserController {

    /**
     * Service layer dependency for business logic
     * Injected via constructor (recommended approach)
     */
    private final UserService userService;

    /**
     * Register a new user
     *
     * This endpoint demonstrates:
     * - POST method for resource creation
     * - Request body validation with @Valid
     * - Proper HTTP status codes (201 Created)
     * - OpenAPI documentation for API consumers
     * - Error handling for business rule violations
     *
     * @param request validated user registration data
     * @return ResponseEntity with created user data and HTTP 201 status
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with username and email. " +
                    "Triggers welcome email and bonus granting through event-driven architecture."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or username/email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Username 'john_doe' is already taken\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validation errors in request data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"username\": \"Username must be between 3 and 20 characters\"}")
                    )
            )
    })
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody
            @Parameter(description = "User registration details", required = true)
            UserRegistrationRequest request) {

        log.info("Received user registration request for username: {} and email: {}",
                request.getUsername(), request.getEmail());

        try {
            // Call service layer to handle business logic
            User registeredUser = userService.registerUser(request.getUsername(), request.getEmail());

            // Convert domain object to response DTO
            UserResponse response = UserResponse.fromUser(registeredUser);

            log.info("User registration successful for username: {} with ID: {}",
                    registeredUser.getUsername(), registeredUser.getId());

            // Return 201 Created with user data
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // Handle business rule violations (username/email already exists)
            log.warn("User registration failed: {}", e.getMessage());

            // In a real application, you might want to return a structured error response
            // instead of relying on global exception handlers
            throw e; // Let global exception handler deal with it
        }
    }

    /**
     * Get user by ID
     *
     * This endpoint demonstrates:
     * - GET method for resource retrieval
     * - Path variable extraction
     * - Optional handling for not-found scenarios
     * - Proper HTTP status codes (200 OK, 404 Not Found)
     *
     * @param id the user ID to retrieve
     * @return ResponseEntity with user data or 404 if not found
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves user information by their unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found and returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found with the specified ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"User not found with ID: 123\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user ID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Invalid user ID format\"}")
                    )
            )
    })
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable
            @Parameter(description = "User ID", required = true, example = "1")
            Long id) {

        log.debug("Received request to get user by ID: {}", id);

        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isPresent()) {
            UserResponse response = UserResponse.fromUser(userOptional.get());
            log.debug("User found with ID: {}", id);
            return ResponseEntity.ok(response);
        } else {
            log.debug("User not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get user by username
     *
     * Additional endpoint for user lookup by username
     * Demonstrates query parameter usage
     *
     * @param username the username to search for
     * @return ResponseEntity with user data or 404 if not found
     */
    @GetMapping
    @Operation(
            summary = "Get user by username",
            description = "Retrieves user information by their username"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found and returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found with the specified username"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Username parameter is required"
            )
    })
    public ResponseEntity<UserResponse> getUserByUsername(
            @RequestParam
            @Parameter(description = "Username to search for", required = true, example = "john_doe")
            String username) {

        log.debug("Received request to get user by username: {}", username);

        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isPresent()) {
            UserResponse response = UserResponse.fromUser(userOptional.get());
            log.debug("User found with username: {}", username);
            return ResponseEntity.ok(response);
        } else {
            log.debug("User not found with username: {}", username);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check username availability
     *
     * Utility endpoint to check if a username is available before registration
     * Demonstrates boolean response patterns
     *
     * @param username the username to check
     * @return ResponseEntity with availability status
     */
    @GetMapping("/check-username")
    @Operation(
            summary = "Check username availability",
            description = "Check if a username is available for registration"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Username availability checked successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"available\": true}")
                    )
            )
    })
    public ResponseEntity<Boolean> checkUsernameAvailability(
            @RequestParam
            @Parameter(description = "Username to check availability", required = true)
            String username) {

        log.debug("Checking username availability for: {}", username);

        boolean available = userService.isUsernameAvailable(username);

        log.debug("Username '{}' availability: {}", username, available);

        return ResponseEntity.ok(available);
    }

    /**
     * Get total user count
     *
     * Administrative endpoint for system statistics
     * Demonstrates simple value responses
     *
     * @return ResponseEntity with total user count
     */
    @GetMapping("/count")
    @Operation(
            summary = "Get total user count",
            description = "Returns the total number of registered users in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User count retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "42")
                    )
            )
    })
    public ResponseEntity<Long> getUserCount() {
        log.debug("Received request for total user count");

        long count = userService.getUserCount();

        log.debug("Total user count: {}", count);

        return ResponseEntity.ok(count);
    }
}
