package com.codehacks.user_registration.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * UserRegisteredEvent - Domain Event for User Registration
 *
 * This class demonstrates:
 * 1. Event-Driven Architecture (EDA) principles
 * 2. Spring's ApplicationEvent system
 * 3. Decoupling business logic through events
 *
 * Event-Driven Architecture Benefits:
 * - Loose Coupling: Event publishers don't know about event listeners
 * - Scalability: Easy to add new listeners without modifying existing code
 * - Responsiveness: Multiple actions can be triggered by a single event
 * - Maintainability: Business logic is separated into focused components
 *
 * Spring Events Explained:
 * - ApplicationEvent: Base class for all Spring events
 * - Events are published using ApplicationEventPublisher
 * - Events are consumed using @EventListener methods
 * - Events are processed synchronously by default in the same thread
 *
 * Domain Events in DDD (Domain-Driven Design):
 * - Represent something important that happened in the domain
 * - Capture the intent and meaning of business operations
 * - Enable reactive programming patterns
 */
@Getter
public class UserRegisteredEvent extends ApplicationEvent {

    /**
     * User ID of the newly registered user
     * Essential for listeners that need to perform user-specific operations
     */
    private final Long userId;

    /**
     * Username of the registered user
     * Used for personalized messages and logging
     */
    private final String username;

    /**
     * Email address of the registered user
     * Used for email notifications and communications
     */
    private final String email;

    /**
     * Timestamp when the registration occurred
     * Useful for auditing and time-based business logic
     */
    private final LocalDateTime registrationTime;

    /**
     * Constructor for UserRegisteredEvent
     *
     * @param source the object on which the event initially occurred (typically the service)
     * @param userId the ID of the newly registered user
     * @param username the username of the registered user
     * @param email the email of the registered user
     *
     * Note: The 'source' parameter is required by ApplicationEvent
     * It represents the component that published the event
     */
    public UserRegisteredEvent(Object source, Long userId, String username, String email) {
        super(source); // Call parent constructor with event source
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.registrationTime = LocalDateTime.now();
    }

    /**
     * Factory method for creating UserRegisteredEvent from User entity
     * This provides a convenient way to create events from domain objects
     *
     * @param source the event source (typically the service publishing the event)
     * @param user the User entity that was registered
     * @return UserRegisteredEvent instance
     */
    public static UserRegisteredEvent fromUser(Object source, com.codehacks.user_registration.model.User user) {
        return new UserRegisteredEvent(source, user.getId(), user.getUsername(), user.getEmail());
    }

    /**
     * Override toString for better logging and debugging
     * Provides a clear representation of the event for troubleshooting
     */
    @Override
    public String toString() {
        return String.format("UserRegisteredEvent{userId=%d, username='%s', email='%s', registrationTime=%s}",
                userId, username, email, registrationTime);
    }
}
