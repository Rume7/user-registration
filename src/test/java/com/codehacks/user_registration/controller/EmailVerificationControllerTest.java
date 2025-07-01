package com.codehacks.user_registration.controller;

import com.codehacks.user_registration.model.User;
import com.codehacks.user_registration.service.EmailVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for EmailVerificationController
 *
 * Tests cover:
 * - Sending verification emails
 * - Verifying email tokens
 * - Checking verification status
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationControllerTest {

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private EmailVerificationController emailVerificationController;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(emailVerificationController)
                .setControllerAdvice(new com.codehacks.user_registration.exception.GlobalExceptionHandler())
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should send verification email successfully")
    void shouldSendVerificationEmailSuccessfully() throws Exception {
        // Given
        when(emailVerificationService.resendVerificationEmail("test@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/email/send-verification")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Verification email sent successfully to test@example.com"));
    }

    @Test
    @DisplayName("Should return false when verification email not sent")
    void shouldReturnFalseWhenVerificationEmailNotSent() throws Exception {
        // Given
        when(emailVerificationService.resendVerificationEmail("test@example.com")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/email/send-verification")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Verification email was not sent (user not found or already verified)"));
    }

    @Test
    @DisplayName("Should handle exception during verification email sending")
    void shouldHandleExceptionDuringVerificationEmailSending() throws Exception {
        // Given
        when(emailVerificationService.resendVerificationEmail("test@example.com"))
                .thenThrow(new RuntimeException("Email service error"));

        // When & Then
        mockMvc.perform(post("/api/v1/email/send-verification")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Verification email error: Email service error"));
    }

    @Test
    @DisplayName("Should verify email with valid token successfully")
    void shouldVerifyEmailWithValidTokenSuccessfully() throws Exception {
        // Given
        testUser.setEmailVerified(true);
        when(emailVerificationService.verifyEmail("valid-token-123"))
                .thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", "valid-token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully for user testuser"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should return false when token is invalid")
    void shouldReturnFalseWhenTokenIsInvalid() throws Exception {
        // Given
        when(emailVerificationService.verifyEmail("invalid-token"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired verification token"));
    }

    @Test
    @DisplayName("Should handle exception during email verification")
    void shouldHandleExceptionDuringEmailVerification() throws Exception {
        // Given
        when(emailVerificationService.verifyEmail("error-token"))
                .thenThrow(new RuntimeException("Verification error"));

        // When & Then
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", "error-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email verification error: Verification error"));
    }

    @Test
    @DisplayName("Should return verification status when email is verified")
    void shouldReturnVerificationStatusWhenEmailIsVerified() throws Exception {
        // Given
        when(emailVerificationService.userExistsById(1L)).thenReturn(true);
        when(emailVerificationService.isEmailVerified(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/email/verification-status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.message").value("Email is verified"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("Should return verification status when email is not verified")
    void shouldReturnVerificationStatusWhenEmailIsNotVerified() throws Exception {
        // Given
        when(emailVerificationService.userExistsById(1L)).thenReturn(true);
        when(emailVerificationService.isEmailVerified(1L)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/email/verification-status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false))
                .andExpect(jsonPath("$.message").value("Email is not verified"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("Should handle exception during verification status check")
    void shouldHandleExceptionDuringVerificationStatusCheck() throws Exception {
        // Given
        when(emailVerificationService.userExistsById(1L)).thenReturn(true);
        when(emailVerificationService.isEmailVerified(1L))
                .thenThrow(new RuntimeException("Status check error"));

        // When & Then
        mockMvc.perform(get("/api/v1/email/verification-status/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.verified").value(false))
                .andExpect(jsonPath("$.message").value("Error checking verification status: Status check error"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("Should handle missing email parameter")
    void shouldHandleMissingEmailParameter() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/email/send-verification"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle missing token parameter")
    void shouldHandleMissingTokenParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/email/verify-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle empty email parameter")
    void shouldHandleEmptyEmailParameter() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/email/send-verification")
                        .param("email", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle empty token parameter")
    void shouldHandleEmptyTokenParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired verification token"));
    }

    @Test
    @DisplayName("Should handle invalid user ID in verification status")
    void shouldHandleInvalidUserIdInVerificationStatus() throws Exception {
        // Given
        when(emailVerificationService.userExistsById(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/email/verification-status/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("User Registration Error"))
                .andExpect(jsonPath("$.message").value("User not found with ID: 999"));
    }

    @Test
    @DisplayName("Should verify email with already verified user")
    void shouldVerifyEmailWithAlreadyVerifiedUser() throws Exception {
        // Given
        testUser.setEmailVerified(true);
        when(emailVerificationService.verifyEmail("already-verified-token"))
                .thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", "already-verified-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully for user testuser"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("Should handle database connection error during verification")
    void shouldHandleDatabaseConnectionErrorDuringVerification() throws Exception {
        // Given
        when(emailVerificationService.verifyEmail("db-error-token"))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", "db-error-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email verification error: Database connection failed"));
    }

    @Test
    @DisplayName("Should handle email service error during resend")
    void shouldHandleEmailServiceErrorDuringResend() throws Exception {
        // Given
        when(emailVerificationService.resendVerificationEmail("service-error@example.com"))
                .thenThrow(new RuntimeException("SMTP server unavailable"));

        // When & Then
        mockMvc.perform(post("/api/v1/email/send-verification")
                        .param("email", "service-error@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Verification email error: SMTP server unavailable"));
    }

    @Test
    @DisplayName("Should handle repository error during status check")
    void shouldHandleRepositoryErrorDuringStatusCheck() throws Exception {
        // Given
        when(emailVerificationService.userExistsById(1L)).thenReturn(true);
        when(emailVerificationService.isEmailVerified(1L))
                .thenThrow(new RuntimeException("Repository connection error"));

        // When & Then
        mockMvc.perform(get("/api/v1/email/verification-status/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.verified").value(false))
                .andExpect(jsonPath("$.message").value("Error checking verification status: Repository connection error"))
                .andExpect(jsonPath("$.userId").value(1));
    }
} 