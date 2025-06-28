package com.codehacks.user_registration.listener;

import com.codehacks.user_registration.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * EmailNotificationListener - Event Listener for sending welcome emails
 *
 * This class demonstrates:
 * 1. Event-driven architecture with Spring's @EventListener
 * 2. Decoupling of business logic from side effects
 * 3. Asynchronous event processing (optional)
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
@Slf4j
public class EmailNotificationListener {

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
            // Simulate email sending process
            sendWelcomeEmail(event.getEmail(), event.getUsername());

            log.info("✅ Welcome email successfully sent to {} for user {}",
                    event.getEmail(), event.getUsername());

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
            sendWelcomeEmail(event.getEmail(), event.getUsername());
            log.info("✅ Async welcome email sent to {}", event.getEmail());
        } catch (Exception e) {
            log.error("❌ Async email sending failed: {}", e.getMessage());
        }
    }
    */

    /**
     * Simulate sending a welcome email
     *
     * In a real application, this method would:
     * 1. Connect to an email service (SendGrid, AWS SES, SMTP server)
     * 2. Load email templates from resources or database
     * 3. Personalize the email content with user data
     * 4. Handle email delivery failures and retries
     * 5. Track email delivery status and user engagement
     *
     * @param email recipient email address
     * @param username recipient username for personalization
     */
    private void sendWelcomeEmail(String email, String username) {
        // Simulate email processing time
        try {
            Thread.sleep(100); // Simulate network call to email service
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Email sending interrupted", e);
        }

        // In a real implementation, you would:
        /*
        EmailRequest emailRequest = EmailRequest.builder()
            .to(email)
            .subject("Welcome to Our Platform!")
            .template("welcome-email")
            .variables(Map.of(
                "username", username,
                "platformName", "Event-Driven App",
                "supportEmail", "support@example.com"
            ))
            .build();

        emailService.sendEmail(emailRequest);
        */

        log.info("📩 Simulated sending welcome email to: {}", email);
        log.info("📋 Email content: 'Welcome {}! Thanks for joining our platform. " +
                "Your account is now active and ready to use.'", username);
    }

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
