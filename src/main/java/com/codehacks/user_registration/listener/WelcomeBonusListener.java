package com.codehacks.user_registration.listener;

import com.codehacks.user_registration.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * WelcomeBonusListener - Event Listener for granting welcome bonuses
 *
 * This class demonstrates:
 * 1. Independent event processing (decoupled from other listeners)
 * 2. Business logic separation through events
 * 3. How multiple listeners can react to the same event
 * 4. Event listener ordering and coordination
 *
 * Key Principles of Event-Driven Architecture:
 * - Single Responsibility: This listener only handles welcome bonus logic
 * - Open/Closed Principle: New bonus types can be added without modifying existing code
 * - Decoupling: This listener doesn't know about EmailNotificationListener or UserService
 * - Extensibility: Easy to add conditions, different bonus types, or external integrations
 *
 * This listener is completely independent and demonstrates how event-driven
 * architecture enables building modular, maintainable applications.
 */
@Component
@Slf4j
public class WelcomeBonusListener {

    /**
     * Handle UserRegisteredEvent by granting a welcome bonus
     *
     * Event Processing Characteristics:
     * - Runs in the same transaction as the user registration (synchronous)
     * - Executes after EmailNotificationListener due to @Order(2)
     * - Independent failure handling (doesn't affect other listeners)
     * - Can access all event data without coupling to other components
     *
     * @param event the UserRegisteredEvent containing user registration details
     */
    @EventListener
    @Order(2) // Execute after email notification (Order(1))
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("🎁 Received UserRegisteredEvent for welcome bonus processing: user {} (ID: {})",
                event.getUsername(), event.getUserId());

        try {
            // Grant welcome bonus to the new user
            grantWelcomeBonus(event.getUserId(), event.getUsername());

            log.info("✅ Welcome bonus successfully granted to user {} (ID: {})",
                    event.getUsername(), event.getUserId());

        } catch (Exception e) {
            // Handle bonus granting failures gracefully
            // In a real application, you might:
            // 1. Retry bonus granting with exponential backoff
            // 2. Queue the bonus for manual processing
            // 3. Send notification to support team
            // 4. Log detailed error information for debugging
            log.error("❌ Failed to grant welcome bonus to user {} (ID: {}): {}",
                    event.getUsername(), event.getUserId(), e.getMessage());

            // Note: We don't re-throw the exception because:
            // - Bonus failure shouldn't rollback user registration
            // - Other listeners should still be able to process the event
            // - User can still use the platform without the bonus
        }
    }

    /**
     * Grant welcome bonus to a newly registered user
     *
     * In a real application, this method would:
     * 1. Connect to a rewards/points system or database
     * 2. Check bonus eligibility rules (first-time users, promotional periods)
     * 3. Calculate bonus amount based on current promotions
     * 4. Create a transaction record for auditing
     * 5. Update user's account balance or points
     * 6. Send bonus confirmation notification
     *
     * Business Rules Examples:
     * - Standard welcome bonus: 100 points
     * - Weekend registration bonus: 150 points
     * - Referral bonus: additional 50 points
     * - VIP users: 200 points
     *
     * @param userId the ID of the user receiving the bonus
     * @param username the username for logging and personalization
     */
    private void grantWelcomeBonus(Long userId, String username) {
        // Simulate bonus processing time
        try {
            Thread.sleep(50); // Simulate database/external service call
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Bonus processing interrupted", e);
        }

        // Simulate business logic for bonus calculation
        int bonusAmount = calculateWelcomeBonus(userId);

        // In a real implementation, you would:
        /*
        BonusTransaction bonusTransaction = BonusTransaction.builder()
            .userId(userId)
            .amount(bonusAmount)
            .type(BonusType.WELCOME)
            .description("Welcome bonus for new user registration")
            .createdAt(LocalDateTime.now())
            .build();

        bonusService.grantBonus(bonusTransaction);

        // Update user's total points/balance
        userAccountService.addPoints(userId, bonusAmount);

        // Send bonus notification
        notificationService.sendBonusNotification(userId, bonusAmount);
        */

        log.info("🎉 Simulated granting {} points welcome bonus to user {} (ID: {})",
                bonusAmount, username, userId);
        log.info("💰 User {} now has access to welcome bonus features", username);
    }

    /**
     * Calculate welcome bonus amount based on business rules
     *
     * This method demonstrates how you can implement complex business logic
     * within event listeners while keeping them focused and single-purpose
     *
     * @param userId the user ID for bonus calculation
     * @return calculated bonus amount
     */
    private int calculateWelcomeBonus(Long userId) {
        // Base welcome bonus
        int baseBonus = 100;

        // Simulate dynamic bonus calculation
        // In reality, this might consider:
        // - Current promotions
        // - User registration source
        // - Time of registration
        // - Referral information
        // - User tier/category

        // Example: Weekend bonus
        java.time.DayOfWeek today = java.time.LocalDate.now().getDayOfWeek();
        if (today == java.time.DayOfWeek.SATURDAY || today == java.time.DayOfWeek.SUNDAY) {
            log.info("🎊 Weekend registration detected! Applying weekend bonus multiplier");
            return (int) (baseBonus * 1.5); // 50% weekend bonus
        }

        return baseBonus;
    }

    /**
     * Additional event handler for premium users
     * This demonstrates conditional event processing
     */
    /*
    @EventListener
    @Order(3)
    public void handlePremiumUserBonus(UserRegisteredEvent event) {
        // Check if user qualifies for premium bonus
        if (isPremiumUser(event.getEmail())) {
            log.info("👑 Granting premium user bonus to: {}", event.getUsername());
            grantPremiumBonus(event.getUserId());
        }
    }

    private boolean isPremiumUser(String email) {
        // Check if email domain indicates premium user (e.g., corporate email)
        return email.endsWith("@enterprise.com") || email.endsWith("@premium.com");
    }

    private void grantPremiumBonus(Long userId) {
        // Grant additional bonus for premium users
        log.info("💎 Premium bonus granted to user ID: {}", userId);
    }
    */
}