package com.codehacks.user_registration.controller;

import com.codehacks.user_registration.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for EmailController
 *
 */
@ExtendWith(MockitoExtension.class)
class EmailControllerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailController emailController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(emailController)
                .setControllerAdvice(new com.codehacks.user_registration.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should test email service successfully")
    void shouldTestEmailServiceSuccessfully() throws Exception {
        // Given
        when(emailService.testEmailService()).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/email/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email service is working correctly"));

        verify(emailService).testEmailService();
    }

    @Test
    @DisplayName("Should return false when email service test fails")
    void shouldReturnFalseWhenEmailServiceTestFails() throws Exception {
        // Given
        when(emailService.testEmailService()).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/email/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email service test failed"));

        verify(emailService).testEmailService();
    }

    @Test
    @DisplayName("Should handle email service test exception")
    void shouldHandleEmailServiceTestException() throws Exception {
        // Given
        when(emailService.testEmailService()).thenThrow(new RuntimeException("Test error"));

        // When & Then
        mockMvc.perform(post("/api/v1/email/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email service error: Test error"));

        verify(emailService).testEmailService();
    }

    @Test
    @DisplayName("Should send welcome email successfully")
    void shouldSendWelcomeEmailSuccessfully() throws Exception {
        // Given
        when(emailService.sendWelcomeEmail("test@example.com", "testuser")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/email/send-welcome")
                        .param("email", "test@example.com")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Welcome email sent successfully to test@example.com"));

        verify(emailService).sendWelcomeEmail("test@example.com", "testuser");
    }

    @Test
    @DisplayName("Should return false when welcome email sending fails")
    void shouldReturnFalseWhenWelcomeEmailSendingFails() throws Exception {
        // Given
        when(emailService.sendWelcomeEmail("test@example.com", "testuser")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/email/send-welcome")
                        .param("email", "test@example.com")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Welcome email was not sent (email service disabled or failed)"));

        verify(emailService).sendWelcomeEmail("test@example.com", "testuser");
    }

    @Test
    @DisplayName("Should handle welcome email sending exception")
    void shouldHandleWelcomeEmailSendingException() throws Exception {
        // Given
        when(emailService.sendWelcomeEmail("test@example.com", "testuser"))
                .thenThrow(new RuntimeException("Email error"));

        // When & Then
        mockMvc.perform(post("/api/v1/email/send-welcome")
                        .param("email", "test@example.com")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email sending error: Email error"));

        verify(emailService).sendWelcomeEmail("test@example.com", "testuser");
    }

    @Test
    @DisplayName("Should get email service status when enabled")
    void shouldGetEmailServiceStatusWhenEnabled() throws Exception {
        // Given
        when(emailService.isEmailEnabled()).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/email/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.message").value("Email service is active"));

        verify(emailService).isEmailEnabled();
    }

    @Test
    @DisplayName("Should get email service status when disabled")
    void shouldGetEmailServiceStatusWhenDisabled() throws Exception {
        // Given
        when(emailService.isEmailEnabled()).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/email/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.status").value("disabled"))
                .andExpect(jsonPath("$.message").value("Email service is disabled"));

        verify(emailService).isEmailEnabled();
    }

    @Test
    @DisplayName("Should handle missing email parameter in welcome email")
    void shouldHandleMissingEmailParameterInWelcomeEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/email/send-welcome")
                        .param("username", "testuser"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle missing username parameter in welcome email")
    void shouldHandleMissingUsernameParameterInWelcomeEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/email/send-welcome")
                        .param("email", "test@example.com"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle empty email parameter in welcome email")
    void shouldHandleEmptyEmailParameterInWelcomeEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/email/send-welcome")
                        .param("email", "")
                        .param("username", "testuser"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle empty username parameter in welcome email")
    void shouldHandleEmptyUsernameParameterInWelcomeEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/email/send-welcome")
                        .param("email", "test@example.com")
                        .param("username", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle invalid email format in welcome email")
    void shouldHandleInvalidEmailFormatInWelcomeEmail() throws Exception {
        // Given
        when(emailService.sendWelcomeEmail("invalid-email", "testuser"))
                .thenThrow(new RuntimeException("Invalid email format"));

        // When & Then
        mockMvc.perform(post("/api/v1/email/send-welcome")
                        .param("email", "invalid-email")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email sending error: Invalid email format"));

        verify(emailService).sendWelcomeEmail("invalid-email", "testuser");
    }

    @Test
    @DisplayName("Should handle email service disabled during welcome email")
    void shouldHandleEmailServiceDisabledDuringWelcomeEmail() throws Exception {
        // Given
        when(emailService.sendWelcomeEmail("test@example.com", "testuser")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/email/send-welcome")
                        .param("email", "test@example.com")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Welcome email was not sent (email service disabled or failed)"));

        verify(emailService).sendWelcomeEmail("test@example.com", "testuser");
    }

    @Test
    @DisplayName("Should handle email service authentication failure")
    void shouldHandleEmailServiceAuthenticationFailure() throws Exception {
        // Given
        when(emailService.testEmailService())
                .thenThrow(new RuntimeException("Authentication failed"));

        // When & Then
        mockMvc.perform(post("/api/v1/email/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email service error: Authentication failed"));

        verify(emailService).testEmailService();
    }

    @Test
    @DisplayName("Should handle email service connection failure")
    void shouldHandleEmailServiceConnectionFailure() throws Exception {
        // Given
        when(emailService.testEmailService())
                .thenThrow(new RuntimeException("Connection timeout"));

        // When & Then
        mockMvc.perform(post("/api/v1/email/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email service error: Connection timeout"));

        verify(emailService).testEmailService();
    }
} 