package com.codehacks.user_registration.service;

import com.codehacks.user_registration.model.User;
import com.codehacks.user_registration.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailVerificationService
 *
 * Tests cover:
 * - Token generation
 * - Email sending
 * - Email verification
 * - Verification status checking
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private User testUser;
    private String testToken;

    @BeforeEach
    void setUp() {
        // Set up test configuration
        ReflectionTestUtils.setField(emailVerificationService, "tokenExpiryHours", 24);
        ReflectionTestUtils.setField(emailVerificationService, "baseUrl", "http://localhost:9090");

        // Create test user
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        testToken = "test-verification-token-123";
    }

    @Test
    @DisplayName("Should generate secure verification token")
    void shouldGenerateSecureVerificationToken() {
        // When
        String token1 = emailVerificationService.generateVerificationToken();
        String token2 = emailVerificationService.generateVerificationToken();

        // Then
        assertThat(token1).isNotNull();
        assertThat(token1).isNotEmpty();
        assertThat(token1).hasSizeGreaterThan(20); // Base64 encoded 32 bytes
        assertThat(token1).isNotEqualTo(token2); // Tokens should be different
        assertThat(token1).matches("^[A-Za-z0-9_-]+$"); // Base64 URL safe characters
    }

    @Test
    @DisplayName("Should send verification email successfully")
    void shouldSendVerificationEmailSuccessfully() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(emailService.sendVerificationEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        emailVerificationService.sendVerificationEmail(testUser);

        // Then
        verify(userRepository).save(testUser);
        verify(emailService).sendVerificationEmail(
                eq("test@example.com"),
                eq("testuser"),
                contains("http://localhost:9090/api/v1/verify-email?token=")
        );

        assertThat(testUser.getVerificationToken()).isNotNull();
        assertThat(testUser.getVerificationTokenExpiry()).isNotNull();
        assertThat(testUser.getVerificationTokenExpiry()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should throw exception when email sending fails")
    void shouldThrowExceptionWhenEmailSendingFails() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(emailService.sendVerificationEmail(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Email service error"));

        // When & Then
        assertThatThrownBy(() -> emailVerificationService.sendVerificationEmail(testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send verification email");

        verify(userRepository).save(testUser);
        verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should verify email with valid token")
    void shouldVerifyEmailWithValidToken() {
        // Given
        testUser.setVerificationToken(testToken);
        testUser.setVerificationTokenExpiry(LocalDateTime.now().plusHours(1));
        
        when(userRepository.findByVerificationToken(testToken)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        Optional<User> result = emailVerificationService.verifyEmail(testToken);

        // Then
        assertThat(result).isPresent();
        User verifiedUser = result.get();
        assertThat(verifiedUser.getEmailVerified()).isTrue();
        assertThat(verifiedUser.getVerificationToken()).isNull();
        assertThat(verifiedUser.getVerificationTokenExpiry()).isNull();

        verify(userRepository).findByVerificationToken(testToken);
        verify(userRepository).save(verifiedUser);
    }

    @Test
    @DisplayName("Should return empty when token is invalid")
    void shouldReturnEmptyWhenTokenIsInvalid() {
        // Given
        when(userRepository.findByVerificationToken(testToken)).thenReturn(Optional.empty());

        // When
        Optional<User> result = emailVerificationService.verifyEmail(testToken);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByVerificationToken(testToken);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return empty when token is expired")
    void shouldReturnEmptyWhenTokenIsExpired() {
        // Given
        testUser.setVerificationToken(testToken);
        testUser.setVerificationTokenExpiry(LocalDateTime.now().minusHours(1)); // Expired
        
        when(userRepository.findByVerificationToken(testToken)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = emailVerificationService.verifyEmail(testToken);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByVerificationToken(testToken);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return user when already verified")
    void shouldReturnUserWhenAlreadyVerified() {
        // Given
        testUser.setEmailVerified(true);
        testUser.setVerificationToken(testToken);
        testUser.setVerificationTokenExpiry(LocalDateTime.now().plusHours(1));
        
        when(userRepository.findByVerificationToken(testToken)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = emailVerificationService.verifyEmail(testToken);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmailVerified()).isTrue();
        verify(userRepository).findByVerificationToken(testToken);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return empty when token expiry is null")
    void shouldReturnEmptyWhenTokenExpiryIsNull() {
        // Given
        testUser.setVerificationToken(testToken);
        testUser.setVerificationTokenExpiry(null);
        
        when(userRepository.findByVerificationToken(testToken)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = emailVerificationService.verifyEmail(testToken);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByVerificationToken(testToken);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should resend verification email successfully")
    void shouldResendVerificationEmailSuccessfully() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(emailService.sendVerificationEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        boolean result = emailVerificationService.resendVerificationEmail("test@example.com");

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(testUser);
        verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return false when user not found for resend")
    void shouldReturnFalseWhenUserNotFoundForResend() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        boolean result = emailVerificationService.resendVerificationEmail("nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return false when user already verified for resend")
    void shouldReturnFalseWhenUserAlreadyVerifiedForResend() {
        // Given
        testUser.setEmailVerified(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        boolean result = emailVerificationService.resendVerificationEmail("test@example.com");

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return false when resend email fails")
    void shouldReturnFalseWhenResendEmailFails() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(emailService.sendVerificationEmail(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Email service error"));

        // When
        boolean result = emailVerificationService.resendVerificationEmail("test@example.com");

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(testUser);
        verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return true when email is verified")
    void shouldReturnTrueWhenEmailIsVerified() {
        // Given
        testUser.setEmailVerified(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = emailVerificationService.isEmailVerified(1L);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return false when email is not verified")
    void shouldReturnFalseWhenEmailIsNotVerified() {
        // Given
        testUser.setEmailVerified(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = emailVerificationService.isEmailVerified(1L);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return false when user not found for verification status")
    void shouldReturnFalseWhenUserNotFoundForVerificationStatus() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        boolean result = emailVerificationService.isEmailVerified(999L);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("Should handle exception during verification gracefully")
    void shouldHandleExceptionDuringVerificationGracefully() {
        // Given
        when(userRepository.findByVerificationToken(testToken))
                .thenThrow(new RuntimeException("Database error"));

        // When
        Optional<User> result = emailVerificationService.verifyEmail(testToken);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByVerificationToken(testToken);
    }

    @Test
    @DisplayName("Should handle exception during resend gracefully")
    void shouldHandleExceptionDuringResendGracefully() {
        // Given
        when(userRepository.findByEmail("test@example.com"))
                .thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = emailVerificationService.resendVerificationEmail("test@example.com");

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should handle exception during status check gracefully")
    void shouldHandleExceptionDuringStatusCheckGracefully() {
        // Given
        when(userRepository.findById(1L))
                .thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = emailVerificationService.isEmailVerified(1L);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findById(1L);
    }
} 