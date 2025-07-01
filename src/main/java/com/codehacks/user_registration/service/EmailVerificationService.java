package com.codehacks.user_registration.service;

import com.codehacks.user_registration.model.User;
import com.codehacks.user_registration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling email verification functionality
 * Generates secure verification tokens and manages verification process
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.verification.token.expiry.hours:2}")
    private int tokenExpiryHours;

    @Value("${app.base.url:http://localhost:9090}")
    private String baseUrl;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generate a secure verification token for email verification
     * @return Base64 encoded secure random token
     */
    public String generateVerificationToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Create and send verification email to user
     * @param user The user to send verification email to
     */
    public void sendVerificationEmail(User user) {
        try {
            // Generate verification token
            String verificationToken = generateVerificationToken();
            LocalDateTime expiryTime = LocalDateTime.now().plusHours(tokenExpiryHours);

            // Update user with verification token
            user.setVerificationToken(verificationToken);
            user.setVerificationTokenExpiry(expiryTime);
            userRepository.save(user);

            // Create verification URL
            String verificationUrl = baseUrl + "/api/v1/verify-email?token=" + verificationToken;

            // Send verification email
            emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationUrl);

            log.info("📧 Verification email sent to: {} for user: {}", user.getEmail(), user.getUsername());
        } catch (Exception e) {
            log.error("❌ Failed to send verification email to: {} for user: {}", user.getEmail(), user.getUsername(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Verify user email using verification token
     * @param token The verification token from email link
     * @return Optional containing the verified user if successful
     */
    public Optional<User> verifyEmail(String token) {
        try {
            // Find user by verification token
            Optional<User> userOpt = userRepository.findByVerificationToken(token);
            
            if (userOpt.isEmpty()) {
                log.warn("⚠️ Invalid verification token provided: {}", token);
                return Optional.empty();
            }

            User user = userOpt.get();

            // Check if token is expired
            if (user.getVerificationTokenExpiry() == null || 
                LocalDateTime.now().isAfter(user.getVerificationTokenExpiry())) {
                log.warn("⚠️ Expired verification token for user: {}", user.getUsername());
                return Optional.empty();
            }

            // Check if already verified
            if (user.getEmailVerified()) {
                log.info("ℹ️ User {} is already verified", user.getUsername());
                return Optional.of(user);
            }

            // Mark email as verified and clear token
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setVerificationTokenExpiry(null);
            userRepository.save(user);

            log.info("✅ Email verified successfully for user: {}", user.getUsername());
            return Optional.of(user);

        } catch (Exception e) {
            log.error("❌ Error during email verification for token: {}", token, e);
            return Optional.empty();
        }
    }

    /**
     * Resend verification email to user
     * @param email The email address to resend verification to
     * @return true if email was resent successfully
     */
    public boolean resendVerificationEmail(String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                log.warn("⚠️ User not found for email: {}", email);
                return false;
            }

            User user = userOpt.get();

            if (user.getEmailVerified()) {
                log.info("ℹ️ User {} is already verified", user.getUsername());
                return false;
            }

            sendVerificationEmail(user);
            return true;

        } catch (Exception e) {
            log.error("❌ Failed to resend verification email to: {}", email, e);
            return false;
        }
    }

    /**
     * Check if user email is verified
     * @param userId The user ID to check
     * @return true if email is verified
     */
    public boolean isEmailVerified(Long userId) {
        try {
            return userRepository.findById(userId)
                    .map(User::getEmailVerified)
                    .orElse(false);
        } catch (Exception e) {
            log.error("❌ Error checking verification status for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user email is verified by username
     * @param username The username to check
     * @return true if email is verified
     */
    public boolean isEmailVerifiedByUsername(String username) {
        try {
            return userRepository.findByUsername(username)
                    .map(User::getEmailVerified)
                    .orElse(false);
        } catch (Exception e) {
            log.error("❌ Error checking verification status for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    public boolean userExistsById(Long userId) {
        return userRepository.existsById(userId);
    }

    public boolean userExistsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Check if user email is verified by UUID
     * @param uuid The user UUID to check
     * @return true if email is verified
     */
    public boolean isEmailVerifiedByUuid(UUID uuid) {
        try {
            return userRepository.findByUuid(uuid)
                    .map(User::getEmailVerified)
                    .orElse(false);
        } catch (Exception e) {
            log.error("❌ Error checking verification status for user UUID {}: {}", uuid, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user exists by UUID
     * @param uuid The user UUID to check
     * @return true if user exists
     */
    public boolean userExistsByUuid(UUID uuid) {
        return userRepository.findByUuid(uuid).isPresent();
    }
} 