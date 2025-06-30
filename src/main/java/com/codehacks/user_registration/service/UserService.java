package com.codehacks.user_registration.service;

import com.codehacks.user_registration.event.UserRegisteredEvent;
import com.codehacks.user_registration.exception.UserRegistrationException;
import com.codehacks.user_registration.model.User;
import com.codehacks.user_registration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * UserService - Business Logic Layer for User operations
 *
 * This class demonstrates:
 * 1. Service layer pattern in Spring Boot
 * 2. Event publishing in event-driven architecture
 * 3. Transaction management
 * 4. Constructor injection and dependency management
 * 5. Input validation and error handling with custom exceptions
 *
 * Service Layer Responsibilities:
 * - Contains business logic and rules
 * - Coordinates between different layers (repository, events)
 * - Handles transactions and ensures data consistency
 * - Validates business rules before data persistence
 *
 * Event Publishing Pattern:
 * - Publishes domain events after successful operations
 * - Enables decoupled, reactive behavior
 * - Allows multiple listeners to respond to business events
 */
@Service
@Transactional // All methods in this service run within a transaction by default
@RequiredArgsConstructor // Lombok generates constructor for final fields (dependency injection)
@Slf4j
public class UserService {

    /**
     * Repository for User data access operations
     * Injected via constructor (recommended approach for mandatory dependencies)
     */
    private final UserRepository userRepository;

    /**
     * Spring's event publisher for broadcasting domain events
     * This is the key component that enables event-driven architecture
     *
     * ApplicationEventPublisher allows us to:
     * - Decouple business logic from side effects
     * - Enable reactive programming patterns
     * - Support multiple listeners for the same event
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Register a new user with username and email
     *
     * This method demonstrates the complete flow of:
     * 1. Input validation (business rules)
     * 2. Data persistence
     * 3. Event publishing (triggering side effects)
     *
     * Transaction Behavior:
     * - @Transactional ensures atomicity
     * - If any step fails, the entire operation is rolled back
     * - Events are published only if the transaction commits successfully
     *
     * @param username the desired username (must be unique)
     * @param email the user's email address (must be unique)
     * @return the saved User entity
     * @throws UserRegistrationException if validation fails or user already exists
     */
    public User registerUser(String username, String email) {
        log.info("Starting user registration process for username: {} and email: {}", username, email);

        // Input validation
        validateRegistrationInput(username, email);

        // Business Rule Validation: Username must be unique
        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed: Username '{}' already exists", username);
            throw new UserRegistrationException("Username '" + username + "' is already taken");
        }

        // Business Rule Validation: Email must be unique
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: Email '{}' already exists", email);
            throw new UserRegistrationException("Email '" + email + "' is already registered");
        }

        try {
            User newUser = User.builder()
                    .username(username)
                    .email(email)
                    .build();

            User savedUser = userRepository.save(newUser);
            log.info("User successfully saved with ID: {}", savedUser.getId());

            // Publish domain event after successful persistence
            // This triggers all registered event listeners
            UserRegisteredEvent event = UserRegisteredEvent.fromUser(this, savedUser);
            eventPublisher.publishEvent(event);
            log.info("Published UserRegisteredEvent for user: {}", savedUser.getUsername());

            return savedUser;

        } catch (Exception e) {
            log.error("Error during user registration for username: {} and email: {}", username, email, e);
            throw new UserRegistrationException("Failed to register user: " + e.getMessage(), e);
        }
    }

    /**
     * Find a user by their ID
     *
     * @param id the user ID to search for
     * @return Optional containing User if found, empty Optional otherwise
     * @throws UserRegistrationException if ID is invalid
     */
    public Optional<User> findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new UserRegistrationException("Invalid user ID: " + id);
        }
        
        return userRepository.findById(id);
    }

    /**
     * Find a user by their username
     *
     * @param username the username to search for
     * @return Optional containing User if found, empty Optional otherwise
     * @throws UserRegistrationException if username is invalid
     */
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        
        if (!StringUtils.hasText(username)) {
            throw new UserRegistrationException("Username cannot be empty or null");
        }
        
        return userRepository.findByUsername(username);
    }

    /**
     * Check if a username is available for registration
     *
     * @param username the username to check
     * @return true if available, false if taken
     * @throws UserRegistrationException if username is invalid
     */
    public boolean isUsernameAvailable(String username) {
        if (!StringUtils.hasText(username)) {
            throw new UserRegistrationException("Username cannot be empty or null");
        }
        
        return !userRepository.existsByUsername(username);
    }

    /**
     * Check if an email is available for registration
     *
     * @param email the email to check
     * @return true if available, false if taken
     * @throws UserRegistrationException if email is invalid
     */
    public boolean isEmailAvailable(String email) {
        if (!StringUtils.hasText(email)) {
            throw new UserRegistrationException("Email cannot be empty or null");
        }
        
        return !userRepository.existsByEmail(email);
    }

    /**
     * Get total count of registered users
     * Useful for analytics and monitoring
     *
     * @return total number of users in the system
     */
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * Validate registration input parameters
     *
     * @param username the username to validate
     * @param email the email to validate
     * @throws UserRegistrationException if validation fails
     */
    private void validateRegistrationInput(String username, String email) {
        if (!StringUtils.hasText(username)) {
            throw new UserRegistrationException("Username cannot be empty or null");
        }
        
        if (username.length() < 3) {
            throw new UserRegistrationException("Username must be at least 3 characters long");
        }
        
        if (username.length() > 30) {
            throw new UserRegistrationException("Username cannot exceed 30 characters");
        }
        
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new UserRegistrationException("Username can only contain letters, numbers, and underscores");
        }

        if (!StringUtils.hasText(email)) {
            throw new UserRegistrationException("Email cannot be empty or null");
        }
        
        if (email.length() > 255) {
            throw new UserRegistrationException("Email cannot exceed 255 characters");
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new UserRegistrationException("Invalid email format");
        }
    }
}
