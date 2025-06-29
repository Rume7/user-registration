package com.codehacks.user_registration.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ErrorResponse - Standardized error response format
 *
 * This class provides a consistent error response structure across the application.
 * It includes all necessary information for debugging and user feedback.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp when the error occurred
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * HTTP status code
     */
    private int status;

    /**
     * Error type/category
     */
    private String error;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Request path that caused the error
     */
    private String path;

    /**
     * Additional error details (for validation errors, etc.)
     */
    private Map<String, String> details;

    /**
     * Request ID for tracking (optional)
     */
    private String requestId;

    /**
     * Stack trace (only in development mode)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String stackTrace;
} 