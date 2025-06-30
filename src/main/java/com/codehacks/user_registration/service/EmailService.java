package com.codehacks.user_registration.service;

import com.codehacks.user_registration.exception.UserRegistrationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

/**
 * EmailService - Service for sending emails using Jakarta Mail
 *
 * This class demonstrates:
 * 1. Spring Boot Mail integration with Jakarta Mail
 * 2. HTML email template processing with Thymeleaf
 * 3. Email configuration and error handling with custom exceptions
 * 4. Service layer pattern for email operations
 *
 * Features:
 * - HTML email templates with Thymeleaf
 * - Welcome email functionality
 * - Comprehensive error handling and logging
 * - Configurable email settings
 * - Email service health checks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.from-name:User Registration Team}")
    private String fromName;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send welcome email to newly registered user
     *
     * @param toEmail recipient email address
     * @param username recipient username for personalization
     * @return true if email sent successfully, false otherwise
     * @throws UserRegistrationException if email sending fails
     */
    public boolean sendWelcomeEmail(String toEmail, String username) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping welcome email to: {}", toEmail);
            return true;
        }

        validateEmailInput(toEmail, username);

        try {
            String subject = "Welcome to Our Platform!";
            String templateName = "welcome-email";
            
            Map<String, Object> templateVariables = Map.of(
                "username", username,
                "platformName", "Event-Driven User Registration",
                "supportEmail", "support@example.com",
                "loginUrl", "http://localhost:${SERVER_PORT:8080}/login"
            );

            sendHtmlEmail(toEmail, subject, templateName, templateVariables);
            
            log.info("✅ Welcome email sent successfully to: {}", toEmail);
            return true;

        } catch (MailAuthenticationException e) {
            log.error("❌ Email authentication failed for {}: {}", toEmail, e.getMessage());
            throw new UserRegistrationException("Email service authentication failed", e);
        } catch (MailSendException e) {
            log.error("❌ Email send failed for {}: {}", toEmail, e.getMessage());
            throw new UserRegistrationException("Failed to send email", e);
        } catch (MailException e) {
            log.error("❌ Mail service error for {}: {}", toEmail, e.getMessage());
            throw new UserRegistrationException("Email service error", e);
        } catch (Exception e) {
            log.error("❌ Unexpected error sending welcome email to {}: {}", toEmail, e.getMessage());
            throw new UserRegistrationException("Unexpected error during email sending", e);
        }
    }

    /**
     * Send HTML email using Thymeleaf template
     *
     * @param toEmail recipient email address
     * @param subject email subject
     * @param templateName Thymeleaf template name
     * @param variables template variables
     * @throws UserRegistrationException if email sending fails
     */
    public void sendHtmlEmail(String toEmail, String subject, String templateName, Map<String, Object> variables) {
        validateEmailParameters(toEmail, subject, templateName);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set email properties
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            // Process Thymeleaf template
            Context context = new Context();
            variables.forEach(context::setVariable);
            String htmlContent = templateEngine.process(templateName, context);

            helper.setText(htmlContent, true); // true = HTML content

            // Send email
            mailSender.send(message);
            
            log.info("📧 HTML email sent to: {} with subject: {}", toEmail, subject);
            
        } catch (MessagingException e) {
            log.error("❌ Messaging error sending HTML email to {}: {}", toEmail, e.getMessage());
            throw new UserRegistrationException("Email messaging error", e);
        } catch (Exception e) {
            log.error("❌ Error processing HTML email template for {}: {}", toEmail, e.getMessage());
            throw new UserRegistrationException("Email template processing error", e);
        }
    }

    /**
     * Send simple text email
     *
     * @param toEmail recipient email address
     * @param subject email subject
     * @param textContent email body text
     * @throws UserRegistrationException if email sending fails
     */
    public void sendTextEmail(String toEmail, String subject, String textContent) {
        validateEmailParameters(toEmail, subject, textContent);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(textContent, false); // false = text content

            mailSender.send(message);
            
            log.info("📧 Text email sent to: {} with subject: {}", toEmail, subject);
            
        } catch (MessagingException e) {
            log.error("❌ Messaging error sending text email to {}: {}", toEmail, e.getMessage());
            throw new UserRegistrationException("Email messaging error", e);
        } catch (Exception e) {
            log.error("❌ Error sending text email to {}: {}", toEmail, e.getMessage());
            throw new UserRegistrationException("Email sending error", e);
        }
    }

    /**
     * Test email service connectivity
     *
     * @return true if email service is working, false otherwise
     */
    public boolean testEmailService() {
        if (!emailEnabled) {
            log.info("Email service is disabled");
            return false;
        }

        try {
            // Try to send a test email to verify configuration
            String testEmail = "test@example.com";
            String subject = "Email Service Test";
            String content = "This is a test email to verify email service configuration.";
            
            sendTextEmail(testEmail, subject, content);
            log.info("✅ Email service test successful");
            return true;
            
        } catch (MailAuthenticationException e) {
            log.error("❌ Email service authentication test failed: {}", e.getMessage());
            return false;
        } catch (MailSendException e) {
            log.error("❌ Email service send test failed: {}", e.getMessage());
            return false;
        } catch (MailException e) {
            log.error("❌ Email service test failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Unexpected error during email service test: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if email service is enabled
     *
     * @return true if email sending is enabled
     */
    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    /**
     * Validate email input parameters for welcome email
     *
     * @param toEmail recipient email address
     * @param username recipient username
     * @throws UserRegistrationException if validation fails
     */
    private void validateEmailInput(String toEmail, String username) {
        if (!StringUtils.hasText(toEmail)) {
            throw new UserRegistrationException("Email address cannot be empty or null");
        }
        
        if (!isValidEmailFormat(toEmail)) {
            throw new UserRegistrationException("Invalid email format: " + toEmail);
        }
        
        if (!StringUtils.hasText(username)) {
            throw new UserRegistrationException("Username cannot be empty or null");
        }
    }

    /**
     * Validate general email parameters
     *
     * @param toEmail recipient email address
     * @param subject email subject
     * @param content email content
     * @throws UserRegistrationException if validation fails
     */
    private void validateEmailParameters(String toEmail, String subject, String content) {
        if (!StringUtils.hasText(toEmail)) {
            throw new UserRegistrationException("Email address cannot be empty or null");
        }
        
        if (!isValidEmailFormat(toEmail)) {
            throw new UserRegistrationException("Invalid email format: " + toEmail);
        }
        
        if (!StringUtils.hasText(subject)) {
            throw new UserRegistrationException("Email subject cannot be empty or null");
        }
        
        if (!StringUtils.hasText(content)) {
            throw new UserRegistrationException("Email content cannot be empty or null");
        }
    }

    /**
     * Validate email format using regex
     *
     * @param email email address to validate
     * @return true if email format is valid
     */
    private boolean isValidEmailFormat(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
} 