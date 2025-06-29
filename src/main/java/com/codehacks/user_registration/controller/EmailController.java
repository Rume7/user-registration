package com.codehacks.user_registration.controller;

import com.codehacks.user_registration.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * EmailController - REST API Controller for Email operations
 *
 * This class demonstrates:
 * 1. Email service testing and health checks
 * 2. Email configuration validation
 * 3. Manual email sending for testing purposes
 * 4. Email service status monitoring
 *
 * Note: This controller is primarily for development and testing.
 * In production, you might want to restrict access to admin users only.
 */
@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Management", description = "APIs for email service testing and management")
public class EmailController {

    private final EmailService emailService;

    /**
     * Test email service connectivity
     *
     * @return ResponseEntity with test result
     */
    @PostMapping("/test")
    @Operation(
            summary = "Test email service",
            description = "Tests the email service connectivity and configuration"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email service test completed",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"success\": true, \"message\": \"Email service is working correctly\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Email service test failed",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"success\": false, \"message\": \"Email service configuration error\"}"
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> testEmailService() {
        log.info("Testing email service connectivity");

        try {
            boolean testResult = emailService.testEmailService();
            
            if (testResult) {
                log.info("✅ Email service test successful");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email service is working correctly"
                ));
            } else {
                log.warn("⚠️ Email service test failed");
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Email service test failed"
                ));
            }

        } catch (Exception e) {
            log.error("❌ Email service test error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Email service error: " + e.getMessage()
            ));
        }
    }

    /**
     * Send a test welcome email
     *
     * @param email recipient email address
     * @param username recipient username
     * @return ResponseEntity with send result
     */
    @PostMapping("/send-welcome")
    @Operation(
            summary = "Send test welcome email",
            description = "Sends a test welcome email to the specified address"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Welcome email sent successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"success\": true, \"message\": \"Welcome email sent successfully\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email parameters",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"success\": false, \"message\": \"Invalid email address\"}"
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> sendTestWelcomeEmail(
            @RequestParam
            @Parameter(description = "Recipient email address", required = true, example = "test@example.com")
            String email,
            
            @RequestParam
            @Parameter(description = "Recipient username", required = true, example = "testuser")
            String username) {

        log.info("Sending test welcome email to: {} for user: {}", email, username);

        try {
            boolean emailSent = emailService.sendWelcomeEmail(email, username);
            
            if (emailSent) {
                log.info("✅ Test welcome email sent successfully to: {}", email);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Welcome email sent successfully to " + email
                ));
            } else {
                log.warn("⚠️ Test welcome email was not sent to: {}", email);
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Welcome email was not sent (email service disabled or failed)"
                ));
            }

        } catch (Exception e) {
            log.error("❌ Test welcome email error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Email sending error: " + e.getMessage()
            ));
        }
    }

    /**
     * Get email service status
     *
     * @return ResponseEntity with email service status
     */
    @GetMapping("/status")
    @Operation(
            summary = "Get email service status",
            description = "Returns the current status of the email service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email service status retrieved",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    example = "{\"enabled\": true, \"status\": \"active\"}"
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> getEmailServiceStatus() {
        log.debug("Getting email service status");

        boolean enabled = emailService.isEmailEnabled();
        String status = enabled ? "active" : "disabled";

        return ResponseEntity.ok(Map.of(
            "enabled", enabled,
            "status", status,
            "message", enabled ? "Email service is active" : "Email service is disabled"
        ));
    }
} 