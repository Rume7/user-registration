package com.codehacks.user_registration.config;

import com.codehacks.user_registration.model.User;
import com.codehacks.user_registration.repository.UserRepository;
import com.codehacks.user_registration.service.EmailService;
import com.codehacks.user_registration.service.EmailVerificationService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Test configuration for integration tests
 * 
 * Provides mock implementations for email services to avoid
 * actual email sending during integration tests
 */
@TestConfiguration
@Profile("integration-test")
public class IntegrationTestConfig {

    /**
     * Mock EmailService that logs instead of sending emails
     */
    @Bean
    @Primary
    public EmailService mockEmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        return new EmailService(mailSender, templateEngine) {
            @Override
            public boolean sendVerificationEmail(String toEmail, String username, String verificationUrl) {
                // Log instead of sending email
                System.out.println("📧 [MOCK] Verification email would be sent to: " + toEmail + 
                                 " for user: " + username + " with URL: " + verificationUrl);
                return true;
            }

            @Override
            public boolean sendWelcomeEmail(String toEmail, String username) {
                // Log instead of sending email
                System.out.println("📧 [MOCK] Welcome email would be sent to: " + toEmail + " for user: " + username);
                return true;
            }

            @Override
            public void sendHtmlEmail(String toEmail, String subject, String templateName, Map<String, Object> variables) {
                // Log instead of sending email
                System.out.println("📧 [MOCK] HTML email would be sent to: " + toEmail + 
                                 " with subject: " + subject + " using template: " + templateName);
            }
        };
    }

    /**
     * Mock EmailVerificationService that doesn't actually send emails
     */
    @Bean
    @Primary
    public EmailVerificationService mockEmailVerificationService(UserRepository userRepository, EmailService emailService) {
        return new EmailVerificationService(userRepository, emailService) {
            @Override
            public void sendVerificationEmail(User user) {
                // Log instead of sending email
                System.out.println("📧 [MOCK] Verification email would be sent to: " + user.getEmail());
                
                // Still generate and save the token for testing purposes
                try {
                    String verificationToken = generateVerificationToken();
                    LocalDateTime expiryTime = LocalDateTime.now().plusHours(2);
                    
                    user.setVerificationToken(verificationToken);
                    user.setVerificationTokenExpiry(expiryTime);
                    userRepository.save(user);
                    
                    System.out.println("📧 [MOCK] Verification token generated: " + verificationToken);
                } catch (Exception e) {
                    System.out.println("❌ [MOCK] Error generating verification token: " + e.getMessage());
                }
            }

            @Override
            public boolean resendVerificationEmail(String email) {
                // Log instead of sending email
                System.out.println("📧 [MOCK] Verification email would be resent to: " + email);
                
                // Still process the resend logic for testing
                try {
                    Optional<User> userOpt = userRepository.findByEmail(email);
                    
                    if (userOpt.isEmpty()) {
                        System.out.println("⚠️ [MOCK] User not found for email: " + email);
                        return false;
                    }

                    User user = userOpt.get();

                    if (user.getEmailVerified()) {
                        System.out.println("ℹ️ [MOCK] User " + user.getUsername() + " is already verified");
                        return false;
                    }

                    sendVerificationEmail(user);
                    return true;
                } catch (Exception e) {
                    System.out.println("❌ [MOCK] Error resending verification email: " + e.getMessage());
                    return false;
                }
            }
        };
    }
} 