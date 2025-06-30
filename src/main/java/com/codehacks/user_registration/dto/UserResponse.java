package com.codehacks.user_registration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserResponse - Data Transfer Object for user registration responses
 *
 * This class demonstrates:
 * 1. Response DTO pattern for API responses
 * 2. JSON serialization control with Jackson annotations
 * 3. Clean separation between internal domain models and API responses
 * 4. Data hiding and security through selective field exposure
 *
 * Response DTO Benefits:
 * - Controls what data is exposed to API clients
 * - Provides stable API contracts independent of domain model changes
 * - Enables API versioning and backward compatibility
 * - Prevents accidental exposure of sensitive data
 * - Allows formatting and transformation of data for client consumption
 *
 * Security Considerations:
 * - Never expose internal IDs that could be used for enumeration attacks
 * - Don't include sensitive data like passwords or internal system information
 * - Consider what information is actually needed by the client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    /**
     * User ID - Primary identifier
     *
     * Exposed for client-side operations and future API calls
     * In high-security scenarios, you might consider using UUIDs
     * or encrypted IDs instead of sequential database IDs
     */
    private Long id;

    /**
     * Username - Public identifier
     *
     * Safe to expose as it's already public information
     * Used for display purposes and user identification
     */
    private String username;

    /**
     * Email address - Contact information
     *
     * Exposed for account management and communication
     * Consider privacy implications - some applications might
     * want to mask or partially hide email addresses
     */
    private String email;

    /**
     * Account creation timestamp
     *
     * JsonFormat annotation controls how LocalDateTime is serialized:
     * - pattern: Defines the date-time format in JSON responses
     * - timezone: Ensures consistent timezone handling (UTC recommended)
     *
     * This provides clients with consistent, readable timestamp format
     * instead of the default ISO-8601 format which can be verbose
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private LocalDateTime createdAt;

    // Additional fields that might be included in responses:

    /**
     * Account status information
     * Useful for showing account state to users
     */
    /*
    private String status; // ACTIVE, PENDING_VERIFICATION, SUSPENDED, etc.
    */

    /**
     * User profile information
     * Only include if relevant to the client application
     */
    /*
    private String fullName;
    private String profilePictureUrl;
    */

    /**
     * Account statistics
     * Useful for user dashboard displays
     */
    /*
    private int totalPoints;
    private LocalDateTime lastLoginAt;
    */

    /**
     * User preferences
     * Include only non-sensitive preferences
     */
    /*
    private boolean emailNotificationsEnabled;
    private String preferredLanguage;
    private String timezone;
    */

    /**
     * Factory method to create UserResponse from User entity
     *
     * This is a common pattern that encapsulates the conversion logic
     * and makes it easy to create response objects from domain entities
     *
     * Benefits:
     * - Centralizes conversion logic
     * - Makes code more readable and maintainable
     * - Ensures consistent field mapping
     * - Easy to modify when response format changes
     *
     * @param user the User entity to convert
     * @return UserResponse DTO
     */
    public static UserResponse fromUser(com.codehacks.user_registration.model.User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Factory method for creating minimal user response (without sensitive data)
     *
     * This could be used in scenarios where you want to return limited information
     * such as in public user listings or search results
     *
     * @param user the User entity to convert
     * @return UserResponse with minimal information
     */
    public static UserResponse fromUserMinimal(com.codehacks.user_registration.model.User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .createdAt(user.getCreatedAt())
                .build();
    }
}