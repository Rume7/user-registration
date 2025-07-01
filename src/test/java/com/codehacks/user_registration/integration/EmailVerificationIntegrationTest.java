package com.codehacks.user_registration.integration;

import com.codehacks.user_registration.config.IntegrationTestConfig;
import com.codehacks.user_registration.dto.UserRegistrationRequest;
import com.codehacks.user_registration.model.User;
import com.codehacks.user_registration.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests specifically for Email Verification functionality
 * 
 * Tests various email verification scenarios using Testcontainers
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("integration-test")
@Testcontainers
@Transactional
@Import(IntegrationTestConfig.class)
class EmailVerificationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.8-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration Test: Complete Email Verification Flow")
    void shouldCompleteEmailVerificationFlow() throws Exception {
        // Step 1: Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("ev_email_verification")
                .email("ev.email.verification@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists());

        // Step 2: Get the user and verify token was generated
        User user = userRepository.findByUsername("ev_email_verification").orElseThrow();
        assertThat(user.getVerificationToken()).isNotNull();
        assertThat(user.getVerificationTokenExpiry()).isNotNull();
        assertThat(user.getEmailVerified()).isFalse();

        // Step 3: Verify email using the token
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", user.getVerificationToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully for user ev_email_verification"));

        // Step 4: Verify user is now marked as verified
        User verifiedUser = userRepository.findByUsername("ev_email_verification").orElseThrow();
        assertThat(verifiedUser.getEmailVerified()).isTrue();
        assertThat(verifiedUser.getVerificationToken()).isNull();
        assertThat(verifiedUser.getVerificationTokenExpiry()).isNull();

        // Step 5: Check verification status
        mockMvc.perform(get("/api/v1/email/verification-status/{userId}", verifiedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.message").value("Email is verified"));
    }

    @Test
    @DisplayName("Integration Test: Email Verification with Already Verified User")
    void shouldHandleAlreadyVerifiedUser() throws Exception {
        // Register and verify a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("ev_already_verified")
                .email("ev.already.verified@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists());

        User user = userRepository.findByUsername("ev_already_verified").orElseThrow();
        String originalToken = user.getVerificationToken(); // Store the original token
        
        // Verify the email
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", originalToken))
                .andExpect(status().isOk());

        // Try to verify again with the same token
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", originalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired verification token"));
    }

    @Test
    @DisplayName("Integration Test: Email Verification with Expired Token")
    void shouldHandleExpiredToken() throws Exception {
        // Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("ev_expired_token")
                .email("ev.expired.token@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists());

        User user = userRepository.findByUsername("ev_expired_token").orElseThrow();
        
        // Manually expire the token
        user.setVerificationTokenExpiry(LocalDateTime.now().minusHours(1));
        userRepository.save(user);

        // Try to verify with expired token
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", user.getVerificationToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired verification token"));
    }

    @Test
    @DisplayName("Integration Test: Resend Verification Email for Unverified User")
    void shouldResendVerificationEmailForUnverifiedUser() throws Exception {
        // Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("ev_resend_unverified")
                .email("ev.resend.unverified@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists());

        User user = userRepository.findByUsername("ev_resend_unverified").orElseThrow();
        String originalToken = user.getVerificationToken();

        // Resend verification email
        mockMvc.perform(post("/api/v1/email/send-verification")
                        .param("email", "ev.resend.unverified@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Verification email sent successfully to ev.resend.unverified@test.com"));

        // Verify a new token was generated
        User updatedUser = userRepository.findByUsername("ev_resend_unverified").orElseThrow();
        assertThat(updatedUser.getVerificationToken()).isNotEqualTo(originalToken);
        assertThat(updatedUser.getVerificationToken()).isNotNull();
        assertThat(updatedUser.getVerificationTokenExpiry()).isNotNull();
    }

    @Test
    @DisplayName("Integration Test: Resend Verification Email for Already Verified User")
    void shouldHandleResendForVerifiedUser() throws Exception {
        // Register and verify a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("ev_resend_verified")
                .email("ev.resend.verified@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists());

        User user = userRepository.findByUsername("ev_resend_verified").orElseThrow();
        
        // Verify the email
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", user.getVerificationToken()))
                .andExpect(status().isOk());

        // Try to resend verification email
        mockMvc.perform(post("/api/v1/email/send-verification")
                        .param("email", "ev.resend.verified@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Verification email was not sent (user not found or already verified)"));
    }

    @Test
    @DisplayName("Integration Test: Resend Verification Email for Non-existent User")
    void shouldHandleResendForNonExistentUser() throws Exception {
        mockMvc.perform(post("/api/v1/email/send-verification")
                        .param("email", "nonexistent@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Verification email was not sent (user not found or already verified)"));
    }

    @Test
    @DisplayName("Integration Test: Email Verification Status for Different Users")
    void shouldCheckVerificationStatusForDifferentUsers() throws Exception {
        // Register two users
        UserRegistrationRequest user1 = UserRegistrationRequest.builder()
                .username("ev_status_user1")
                .email("ev.status1@test.com")
                .build();

        UserRegistrationRequest user2 = UserRegistrationRequest.builder()
                .username("ev_status_user2")
                .email("ev.status2@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists());

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists());

        User unverifiedUser = userRepository.findByUsername("ev_status_user1").orElseThrow();
        User userToVerify = userRepository.findByUsername("ev_status_user2").orElseThrow();

        // Check status of unverified user
        mockMvc.perform(get("/api/v1/email/verification-status/{userId}", unverifiedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false))
                .andExpect(jsonPath("$.message").value("Email is not verified"));

        // Verify the second user
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", userToVerify.getVerificationToken()))
                .andExpect(status().isOk());

        // Check status of verified user
        mockMvc.perform(get("/api/v1/email/verification-status/{userId}", userToVerify.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.message").value("Email is verified"));
    }

    @Test
    @DisplayName("Integration Test: Email Verification with Invalid Token Format")
    void shouldHandleInvalidTokenFormat() throws Exception {
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", "invalid-token-format-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired verification token"));
    }

    @Test
    @DisplayName("Integration Test: Email Verification with Empty Token")
    void shouldHandleEmptyToken() throws Exception {
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired verification token"));
    }

    @Test
    @DisplayName("Integration Test: Email Verification with Null Token")
    void shouldHandleNullToken() throws Exception {
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", (String) null))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Integration Test: Email Verification Status for Non-existent User")
    void shouldHandleVerificationStatusForNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/v1/email/verification-status/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("User Registration Error"))
                .andExpect(jsonPath("$.message").value("User not found with ID: 999"));
    }

    @Test
    @DisplayName("Integration Test: Email Verification Status by UUID")
    void shouldCheckVerificationStatusByUuid() throws Exception {
        // Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("ev_uuid_status_test")
                .email("ev.uuid.status@test.com")
                .build();

        String responseContent = mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract UUID from response
        String uuid = objectMapper.readTree(responseContent).get("uuid").asText();
        UUID userUuid = UUID.fromString(uuid);

        // Check verification status by username
        mockMvc.perform(get("/api/v1/email/verification-status/username/{username}", "ev_uuid_status_test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false))
                .andExpect(jsonPath("$.message").value("Email is not verified"));

        // Check verification status by UUID
        mockMvc.perform(get("/api/v1/email/verification-status/uuid/{uuid}", userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false))
                .andExpect(jsonPath("$.message").value("Email is not verified"))
                .andExpect(jsonPath("$.uuid").value(userUuid.toString()));

        // Verify the email
        User user = userRepository.findByUsername("ev_uuid_status_test").orElseThrow();
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", user.getVerificationToken()))
                .andExpect(status().isOk());

        // Check verification status again by UUID
        mockMvc.perform(get("/api/v1/email/verification-status/uuid/{uuid}", userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.message").value("Email is verified"))
                .andExpect(jsonPath("$.uuid").value(userUuid.toString()));
    }

    @Test
    @DisplayName("Integration Test: Email Verification Status by UUID for Non-existent User")
    void shouldHandleVerificationStatusByUuidForNonExistentUser() throws Exception {
        UUID nonExistentUuid = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/email/verification-status/uuid/{uuid}", nonExistentUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("User Registration Error"))
                .andExpect(jsonPath("$.message").value("User not found with UUID: " + nonExistentUuid));
    }
} 