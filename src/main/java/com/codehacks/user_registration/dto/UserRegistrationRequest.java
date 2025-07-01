package com.codehacks.user_registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserRegistrationRequest - Data Transfer Object for user registration requests
 *
 * This class demonstrates:
 * 1. DTO (Data Transfer Object) pattern for API requests
 * 2. Bean Validation (JSR-303/380) for input validation
 * 3. Clean separation between API layer and domain models
 * 4. Lombok for reducing boilerplate code
 *
 * DTO Pattern Benefits:
 * - Decouples API contracts from internal domain models
 * - Provides validation at the API boundary
 * - Enables API evolution without changing domain models
 * - Improves security by controlling what data is accepted
 *
 * Bean Validation Annotations:
 * - @NotBlank: Ensures field is not null and contains non-whitespace characters
 * - @Email: Validates email format using RFC standards
 * - @Size: Validates string length constraints
 *
 * These validations are automatically triggered by Spring when using @Valid
 * in controller methods, providing early validation before business logic.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationRequest {

    /**
     * Username field with validation constraints
     *
     * Validation Rules:
     * - Cannot be null or empty (@NotBlank)
     * - Must be between 3 and 30 characters (@Size)
     * - Should contain only alphanumeric characters and underscores (additional validation can be added)
     *
     * Business Considerations:
     * - Username is used for login and public identification
     * - Length limits prevent database overflow and UI issues
     * - Character restrictions prevent security issues and ensure URL safety
     */
    @NotBlank(message = "Username is required and cannot be empty")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    // Additional validation pattern can be added:
    // @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    /**
     * Email field with validation constraints
     *
     * Validation Rules:
     * - Cannot be null or empty (@NotBlank)
     * - Must be a valid email format (@Email)
     * - Maximum length of 100 characters to match database constraint
     *
     * Business Considerations:
     * - Email is used for communication and account recovery
     * - Must be unique across the system (enforced at service layer)
     * - Length limit prevents abuse and database issues
     */
    @NotBlank(message = "Email is required and cannot be empty")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    // Additional fields that might be added later:

    /**
     * Optional full name field
     * Commented out for this example but shows how to extend the DTO
     */
    /*
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;
    */

    /**
     * Optional password field
     * In a real application, you'd include password with validation
     */
    /*
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
             message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
    */

    /**
     * Optional terms acceptance field
     * Required for legal compliance in many jurisdictions
     */
    /*
    @AssertTrue(message = "You must accept the terms and conditions")
    private boolean acceptTerms;
    */

    /**
     * Optional newsletter subscription preference
     * Demonstrates how to include user preferences in registration
     */
    /*
    private boolean subscribeToNewsletter = false;
    */

    /**
     * Custom validation method (can be used with @Valid)
     * This shows how to implement cross-field validation
     */
    /*
    @AssertTrue(message = "Password and confirm password must match")
    private boolean isPasswordConfirmed() {
        if (password == null || confirmPassword == null) {
            return true; // Let @NotBlank handle null validation
        }
        return password.equals(confirmPassword);
    }
    */
}