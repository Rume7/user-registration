package com.codehacks.user_registration.exception;

/**
 * UserRegistrationException - Base exception for user registration errors
 */
public class UserRegistrationException extends RuntimeException {

    public UserRegistrationException(String message) {
        super(message);
    }

    public UserRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * UserAlreadyExistsException - Thrown when trying to register a user that already exists
 */
class UserAlreadyExistsException extends UserRegistrationException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String field, String value) {
        super(String.format("User with %s '%s' already exists", field, value));
    }
}

/**
 * UserNotFoundException - Thrown when a user is not found
 */
class UserNotFoundException extends UserRegistrationException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String field, String value) {
        super(String.format("User with %s '%s' not found", field, value));
    }
}

/**
 * EmailServiceException - Thrown when email service operations fail
 */
class EmailServiceException extends UserRegistrationException {

    public EmailServiceException(String message) {
        super(message);
    }

    public EmailServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * ValidationException - Thrown when data validation fails
 */
class ValidationException extends UserRegistrationException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String field, String message) {
        super(String.format("Validation failed for field '%s': %s", field, message));
    }
} 