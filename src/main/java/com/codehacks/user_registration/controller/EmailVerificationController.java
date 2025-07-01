package com.codehacks.user_registration.controller;

import com.codehacks.user_registration.model.User;
import com.codehacks.user_registration.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * EmailVerificationController - REST API Controller for Email Verification operations
 *
 * This class provides endpoints for:
 * 1. Sending verification emails to users
 * 2. Verifying email tokens
 * 3. Resending verification emails
 * 4. Checking email verification status
 */
@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Verification", description = "APIs for email verification functionality")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * Send verification email to user
     *
     * @param email user's email address
     * @return ResponseEntity with send result
     */
    @PostMapping("/send-verification")
    @Operation(
            summary = "Send verification email",
            description = "Sends a verification email to the specified email address"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verification email sent successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"success\": true, \"message\": \"Verification email sent successfully\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email or user not found",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"success\": false, \"message\": \"User not found\"}"
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> sendVerificationEmail(
            @RequestParam
            @Parameter(description = "User's email address", required = true, example = "user@example.com")
            String email) {

        log.info("Sending verification email to: {}", email);

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email parameter is required and cannot be empty"
            ));
        }

        try {
            boolean emailSent = emailVerificationService.resendVerificationEmail(email);
            
            if (emailSent) {
                log.info("✅ Verification email sent successfully to: {}", email);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Verification email sent successfully to " + email
                ));
            } else {
                log.warn("⚠️ Verification email was not sent to: {}", email);
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Verification email was not sent (user not found or already verified)"
                ));
            }

        } catch (Exception e) {
            log.error("❌ Verification email error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Verification email error: " + e.getMessage()
            ));
        }
    }

    /**
     * Verify email using verification token
     *
     * @param token verification token from email
     * @return ResponseEntity with verification result
     */
    @GetMapping("/verify-email")
    @Operation(
            summary = "Verify email address",
            description = "Verifies a user's email address using the verification token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email verified successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"success\": true, \"message\": \"Email verified successfully\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"success\": false, \"message\": \"Invalid or expired token\"}"
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> verifyEmail(
            @RequestParam(required = false) String token) {
        log.info("Verifying email with token: {}", token);
        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Token parameter is required and cannot be empty"
            ));
        }
        if (token.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Invalid or expired verification token"
            ));
        }
        try {
            var userOpt = emailVerificationService.verifyEmail(token);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email verified successfully for user " + user.getUsername(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Invalid or expired verification token"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Email verification error: " + e.getMessage()
            ));
        }
    }

    /**
     * Check if user's email is verified
     *
     * @param userId user ID to check
     * @return ResponseEntity with verification status
     */
    @GetMapping("/verification-status/{userId}")
    @Operation(
            summary = "Check email verification status",
            description = "Checks if a user's email address is verified"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verification status retrieved",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"verified\": true, \"message\": \"Email is verified\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"verified\": false, \"message\": \"User not found\"}"
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> getVerificationStatus(
            @PathVariable Long userId) {
        log.info("Checking email verification status for user ID: {}", userId);
        try {
            boolean exists = emailVerificationService.userExistsById(userId);
            if (!exists) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", 404,
                    "error", "User Registration Error",
                    "message", "User not found with ID: " + userId
                ));
            }
            boolean verified = emailVerificationService.isEmailVerified(userId);
            String message = verified ? "Email is verified" : "Email is not verified";
            return ResponseEntity.ok(Map.of(
                "verified", verified,
                "message", message,
                "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "verified", false,
                "message", "Error checking verification status: " + e.getMessage(),
                "userId", userId
            ));
        }
    }

    /**
     * Check if user's email is verified by username
     *
     * @param username username to check
     * @return ResponseEntity with verification status
     */
    @GetMapping("/verification-status/username/{username}")
    @Operation(
            summary = "Check email verification status by username",
            description = "Checks if a user's email address is verified by their username"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verification status retrieved",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"verified\": true, \"message\": \"Email is verified\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"verified\": false, \"message\": \"User not found\"}"
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> getVerificationStatusByUsername(
            @PathVariable String username) {
        log.info("Checking email verification status for username: {}", username);
        try {
            boolean exists = emailVerificationService.userExistsByUsername(username);
            if (!exists) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", 404,
                    "error", "User Registration Error",
                    "message", "User not found with username: " + username
                ));
            }
            boolean verified = emailVerificationService.isEmailVerifiedByUsername(username);
            String message = verified ? "Email is verified" : "Email is not verified";
            return ResponseEntity.ok(Map.of(
                "verified", verified,
                "message", message,
                "username", username
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "verified", false,
                "message", "Error checking verification status: " + e.getMessage(),
                "username", username
            ));
        }
    }

    /**
     * Check if user's email is verified by UUID
     *
     * @param uuid user UUID to check
     * @return ResponseEntity with verification status
     */
    @GetMapping("/verification-status/uuid/{uuid}")
    @Operation(
            summary = "Check email verification status by UUID",
            description = "Checks if a user's email address is verified by their UUID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verification status retrieved",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"verified\": true, \"message\": \"Email is verified\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"verified\": false, \"message\": \"User not found\"}"
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> getVerificationStatusByUuid(
            @PathVariable UUID uuid) {
        log.info("Checking email verification status for UUID: {}", uuid);
        try {
            boolean exists = emailVerificationService.userExistsByUuid(uuid);
            if (!exists) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", 404,
                    "error", "User Registration Error",
                    "message", "User not found with UUID: " + uuid
                ));
            }
            boolean verified = emailVerificationService.isEmailVerifiedByUuid(uuid);
            String message = verified ? "Email is verified" : "Email is not verified";
            return ResponseEntity.ok(Map.of(
                "verified", verified,
                "message", message,
                "uuid", uuid.toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "verified", false,
                "message", "Error checking verification status: " + e.getMessage(),
                "uuid", uuid.toString()
            ));
        }
    }
} 