package com.codehacks.user_registration.listener;

import com.codehacks.user_registration.event.UserRegisteredEvent;
import com.codehacks.user_registration.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * EmailNotificationListener - Event Listener for sending welcome emails
 *
 * This class demonstrates:
 * 1. Event-driven architecture with Spring's @EventListener
 * 2. Decoupling of business logic from side effects
 * 3. Integration with real email service
 * 4. Event listener ordering and priority
 *
 * Event Listener Concepts:
 * - @EventListener methods are automatically invoked when matching events are published
 * - Multiple listeners can respond to the same event independently
 * - Listeners are decoupled from the event publisher and from each other
 * - Events are processed synchronously by default (same thread as publisher)
 *
 * Benefits of Event-Driven Email Notifications:
 * - User registration logic doesn't need to know about email sending
 * - Easy to add/remove notification features without changing core logic
 * - Can easily switch between different notification providers
 * - Supports multiple notification types (email, SMS, push notifications)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationListener {

    private final EmailService emailService;

    /**
     * Handle UserRegisteredEvent by sending a welcome email
     *
     * @EventListener Annotation Details:
     * - Automatically invoked when UserRegisteredEvent is published
     * - Method parameter type determines which events this listener handles
     * - Spring uses reflection to find and invoke listener methods
     * - Listeners are invoked in the same transaction as the event publisher
     *
     * @Order Annotation:
     * - Controls the execution order when multiple listeners exist
     * - Lower values have higher priority (execute first)
     * - Useful when listeners have dependencies on each other
     *
     * @param event the UserRegisteredEvent containing user details
     */
    @EventListener
    @Order(1) // Execute this listener first (before welcome bonus)
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("📧 Received UserRegisteredEvent for user: {} (ID: {})",
                event.getUsername(), event.getUserId());

        try {
            // Send welcome email using the real email service
            boolean emailSent = emailService.sendWelcomeEmail(event.getEmail(), event.getUsername());

            if (emailSent) {
                log.info("✅ Welcome email successfully sent to {} for user {}",
                        event.getEmail(), event.getUsername());
            } else {
                log.warn("⚠️ Welcome email was not sent to {} for user {} (email service disabled or failed)",
                        event.getEmail(), event.getUsername());
            }

        } catch (Exception e) {
            // In a real application, you might want to:
            // 1. Retry the email sending
            // 2. Store failed emails in a queue for later processing
            // 3. Send alerts to administrators
            // 4. Update user preferences to disable emails
            log.error("❌ Failed to send welcome email to {} for user {}: {}",
                    event.getEmail(), event.getUsername(), e.getMessage());

            // Note: We don't re-throw the exception here because:
            // - It would rollback the user registration transaction
            // - Email failure shouldn't prevent user registration
            // - Other listeners would not be executed
        }
    }

    /**
     * Alternative asynchronous event handler
     *
     * @Async annotation makes this method run in a separate thread:
     * - Non-blocking: doesn't slow down the user registration process
     * - Parallel processing: multiple emails can be sent concurrently
     * - Fault isolation: email failures don't affect user registration
     *
     * Note: To enable @Async, you need to add @EnableAsync to your main application class
     * and configure a TaskExecutor bean for thread pool management
     */
    /*
    @EventListener
    @Async
    @Order(1)
    public void handleUserRegisteredAsync(UserRegisteredEvent event) {
        log.info("📧 Processing UserRegisteredEvent asynchronously for user: {}", event.getUsername());

        try {
            boolean emailSent = emailService.sendWelcomeEmail(event.getEmail(), event.getUsername());
            
            if (emailSent) {
                log.info("✅ Async welcome email sent to {}", event.getEmail());
            } else {
                log.warn("⚠️ Async welcome email was not sent to {}", event.getEmail());
            }
        } catch (Exception e) {
            log.error("❌ Async email sending failed: {}", e.getMessage());
        }
    }
    */

    /**
     * Additional event handler for user email verification
     * This demonstrates how you can have multiple handlers in the same listener class
     */
    /*
    @EventListener
    public void handleUserEmailVerificationRequired(UserRegisteredEvent event) {
        log.info("📨 Sending email verification request to: {}", event.getEmail());
        // Send verification email logic here
    }
    */
}
