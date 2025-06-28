package com.codehacks.user_registration.service;

import com.codehacks.user_registration.event.UserRegisteredEvent;
import com.codehacks.user_registration.model.User;
import com.codehacks.user_registration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * UserService - Business Logic Layer for User operations
 *
 * This class demonstrates:
 * 1. Service layer pattern in Spring Boot
 * 2. Event publishing in event-driven architecture
 * 3. Transaction management
 * 4. Constructor injection and dependency management
 * 5. Input validation and error handling
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
     * @throws IllegalArgumentException if username or email already exists
     */
    public User registerUser(String username, String email) {
        log.info("Starting user registration process for username: {} and email: {}", username, email);

        // Business Rule Validation: Username must be unique
        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed: Username '{}' already exists", username);
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }

        // Business Rule Validation: Email must be unique
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: Email '{}' already exists", email);
            throw new IllegalArgumentException("Email '" + email + "' is already registered");
        }

        // Create new User entity using Builder pattern (provided by Lombok)
        User newUser = User.builder()
                .username(username)
                .email(email)
                // createdAt will be set automatically by @PrePersist in User entity
                .build();

        // Persist the user to database
        // The save operation returns the entity with generated ID
        User savedUser = userRepository.save(newUser);
        log.info("User successfully saved with ID: {}", savedUser.getId());

        // Publish domain event after successful persistence
        // This triggers all registered event listeners
        UserRegisteredEvent event = UserRegisteredEvent.fromUser(this, savedUser);
        eventPublisher.publishEvent(event);
        log.info("Published UserRegisteredEvent for user: {}", savedUser.getUsername());

        return savedUser;
    }

    /**
     * Find a user by their ID
     *
     * @param id the user ID to search for
     * @return Optional containing User if found, empty Optional otherwise
     */
    public Optional<User> findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }

    /**
     * Find a user by their username
     *
     * @param username the username to search for
     * @return Optional containing User if found, empty Optional otherwise
     */
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    /**
     * Check if a username is available for registration
     *
     * @param username the username to check
     * @return true if available, false if taken
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Check if an email is available for registration
     *
     * @param email the email to check
     * @return true if available, false if taken
     */
    public boolean isEmailAvailable(String email) {
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
}
