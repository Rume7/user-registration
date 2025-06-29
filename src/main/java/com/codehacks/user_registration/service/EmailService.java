package com.codehacks.user_registration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
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
 * 3. Email configuration and error handling
 * 4. Service layer pattern for email operations
 *
 * Features:
 * - HTML email templates with Thymeleaf
 * - Welcome email functionality
 * - Error handling and logging
 * - Configurable email settings
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
     */
    public boolean sendWelcomeEmail(String toEmail, String username) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping welcome email to: {}", toEmail);
            return true;
        }

        try {
            String subject = "Welcome to Our Platform!";
            String templateName = "welcome-email";
            
            Map<String, Object> templateVariables = Map.of(
                "username", username,
                "platformName", "Event-Driven User Registration",
                "supportEmail", "support@example.com",
                "loginUrl", "http://localhost:8080/login"
            );

            sendHtmlEmail(toEmail, subject, templateName, templateVariables);
            
            log.info("✅ Welcome email sent successfully to: {}", toEmail);
            return true;

        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Send HTML email using Thymeleaf template
     *
     * @param toEmail recipient email address
     * @param subject email subject
     * @param templateName Thymeleaf template name
     * @param variables template variables
     * @throws MessagingException if email sending fails
     */
    public void sendHtmlEmail(String toEmail, String subject, String templateName, Map<String, Object> variables) 
            throws MessagingException {
        
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
    }

    /**
     * Send simple text email
     *
     * @param toEmail recipient email address
     * @param subject email subject
     * @param textContent email body text
     * @throws MessagingException if email sending fails
     */
    public void sendTextEmail(String toEmail, String subject, String textContent) 
            throws MessagingException {
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, fromName);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(textContent, false); // false = text content

        mailSender.send(message);
        
        log.info("📧 Text email sent to: {} with subject: {}", toEmail, subject);
    }

    /**
     * Test email service connectivity
     *
     * @return true if email service is working, false otherwise
     */
    public boolean testEmailService() {
        try {
            // Try to send a test email to verify configuration
            String testEmail = "test@example.com";
            String subject = "Email Service Test";
            String content = "This is a test email to verify email service configuration.";
            
            sendTextEmail(testEmail, subject, content);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Email service test failed: {}", e.getMessage());
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
} 