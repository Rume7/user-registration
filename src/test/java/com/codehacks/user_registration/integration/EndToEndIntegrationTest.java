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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Integration tests for the complete user registration system
 * 
 * Tests the complete user journey from registration to email verification
 * using Testcontainers for realistic testing environment
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("integration-test")
@Testcontainers
@Transactional
@Import(IntegrationTestConfig.class)
class EndToEndIntegrationTest {

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
    @DisplayName("Integration Test: Complete User Registration and Verification Journey")
    void shouldCompleteUserRegistrationAndVerificationJourney() throws Exception {
        // Step 1: Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("e2e_test_user")
                .email("e2e_test@example.com")
                .build();

        String responseContent = mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.username").value("e2e_test_user"))
                .andExpect(jsonPath("$.email").value("e2e_test@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Step 2: Verify user was saved in database
        User savedUser = userRepository.findByUsername("e2e_test_user").orElseThrow();
        assertThat(savedUser.getUsername()).isEqualTo("e2e_test_user");
        assertThat(savedUser.getEmail()).isEqualTo("e2e_test@example.com");
        assertThat(savedUser.getEmailVerified()).isFalse();
        assertThat(savedUser.getVerificationToken()).isNotNull();
        assertThat(savedUser.getVerificationTokenExpiry()).isNotNull();

        // Step 3: Verify email using the token
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", savedUser.getVerificationToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 4: Verify user is now marked as verified
        User verifiedUser = userRepository.findByUsername("e2e_test_user").orElseThrow();
        assertThat(verifiedUser.getEmailVerified()).isTrue();
        assertThat(verifiedUser.getVerificationToken()).isNull();
        assertThat(verifiedUser.getVerificationTokenExpiry()).isNull();

        // Step 5: Check verification status via API
        mockMvc.perform(get("/api/v1/email/verification-status/{userId}", verifiedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.message").value("Email is verified"));
    }

    @Test
    @DisplayName("Integration Test: Multiple Users Registration and Verification")
    void shouldHandleMultipleUsersRegistrationAndVerification() throws Exception {
        // Register first user
        UserRegistrationRequest request1 = UserRegistrationRequest.builder()
                .username("user1")
                .email("user1@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists());

        // Register second user
        UserRegistrationRequest request2 = UserRegistrationRequest.builder()
                .username("user2")
                .email("user2@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists());

        // Register third user
        UserRegistrationRequest request3 = UserRegistrationRequest.builder()
                .username("user3")
                .email("user3@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists());

        // Verify all users exist in database
        assertThat(userRepository.findByUsername("user1")).isPresent();
        assertThat(userRepository.findByUsername("user2")).isPresent();
        assertThat(userRepository.findByUsername("user3")).isPresent();

        // Verify first user's email
        User savedUser1 = userRepository.findByUsername("user1").orElseThrow();
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", savedUser1.getVerificationToken()))
                .andExpect(status().isOk());

        // Verify second user's email
        User savedUser2 = userRepository.findByUsername("user2").orElseThrow();
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", savedUser2.getVerificationToken()))
                .andExpect(status().isOk());

        // Check that all users are now verified
        assertThat(userRepository.findByUsername("user1").orElseThrow().getEmailVerified()).isTrue();
        assertThat(userRepository.findByUsername("user2").orElseThrow().getEmailVerified()).isTrue();
        assertThat(userRepository.findByUsername("user3").orElseThrow().getEmailVerified()).isFalse();
    }

    @Test
    @DisplayName("End-to-End Test: User Registration with Validation Errors")
    void shouldHandleValidationErrors() throws Exception {
        // Test empty username
        UserRegistrationRequest emptyUsername = UserRegistrationRequest.builder()
                .username("")
                .email("empty@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUsername)))
                .andExpect(status().isBadRequest());

        // Test invalid email
        UserRegistrationRequest invalidEmail = UserRegistrationRequest.builder()
                .username("invalid_email")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmail)))
                .andExpect(status().isBadRequest());

        // Test short username
        UserRegistrationRequest shortUsername = UserRegistrationRequest.builder()
                .username("ab")
                .email("short@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortUsername)))
                .andExpect(status().isBadRequest());

        // Verify no users were created
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("End-to-End Test: Resend Verification Email Flow")
    void shouldHandleResendVerificationEmailFlow() throws Exception {
        // Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("resend_flow_user")
                .email("resend.flow@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        User user = userRepository.findByUsername("resend_flow_user").orElseThrow();
        String originalToken = user.getVerificationToken();

        // Resend verification email
        mockMvc.perform(post("/api/v1/email/send-verification")
                        .param("email", "resend.flow@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Verification email sent successfully to resend.flow@test.com"));

        // Verify new token was generated
        User updatedUser = userRepository.findByUsername("resend_flow_user").orElseThrow();
        assertThat(updatedUser.getVerificationToken()).isNotEqualTo(originalToken);

        // Verify email with new token
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", updatedUser.getVerificationToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify user is now verified
        User verifiedUser = userRepository.findByUsername("resend_flow_user").orElseThrow();
        assertThat(verifiedUser.getEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("End-to-End Test: Error Handling and Edge Cases")
    void shouldHandleErrorCasesAndEdgeCases() throws Exception {
        // Try to get non-existent user
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("User Registration Error"))
                .andExpect(jsonPath("$.message").value("User not found with ID: 999"));

        // Try to verify with invalid token
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", "invalid-token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired verification token"));

        // Try to resend verification for non-existent user
        mockMvc.perform(post("/api/v1/email/send-verification")
                        .param("email", "nonexistent@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Verification email was not sent (user not found or already verified)"));

        // Check verification status for non-existent user
        mockMvc.perform(get("/api/v1/email/verification-status/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("User Registration Error"))
                .andExpect(jsonPath("$.message").value("User not found with ID: 999"));

        // Verify no users were created
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("End-to-End Test: Database Consistency and Transaction Integrity")
    void shouldMaintainDatabaseConsistency() throws Exception {
        // Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("consistency_test")
                .email("consistency@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        // Verify user exists and has verification token
        User user = userRepository.findByUsername("consistency_test").orElseThrow();
        assertThat(user.getVerificationToken()).isNotNull();
        assertThat(user.getEmailVerified()).isFalse();

        // Verify email
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", user.getVerificationToken()))
                .andExpect(status().isOk());

        // Verify database state is consistent
        User verifiedUser = userRepository.findByUsername("consistency_test").orElseThrow();
        assertThat(verifiedUser.getEmailVerified()).isTrue();
        assertThat(verifiedUser.getVerificationToken()).isNull();
        assertThat(verifiedUser.getVerificationTokenExpiry()).isNull();

        // Verify the same user can be retrieved by different methods
        Optional<User> byId = userRepository.findById(verifiedUser.getId());
        Optional<User> byUsername = userRepository.findByUsername("consistency_test");
        Optional<User> byEmail = userRepository.findByEmail("consistency@test.com");

        assertThat(byId).isPresent();
        assertThat(byUsername).isPresent();
        assertThat(byEmail).isPresent();

        assertThat(byId.get().getId()).isEqualTo(byUsername.get().getId());
        assertThat(byUsername.get().getId()).isEqualTo(byEmail.get().getId());
    }

    @Test
    @DisplayName("Integration Test: UUID-based Operations")
    void shouldHandleUuidBasedOperations() throws Exception {
        // Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("uuid_operations_user")
                .email("uuid.operations@test.com")
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

        // Get user by UUID
        mockMvc.perform(get("/api/v1/users/uuid/{uuid}", userUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("uuid_operations_user"))
                .andExpect(jsonPath("$.email").value("uuid.operations@test.com"))
                .andExpect(jsonPath("$.uuid").value(userUuid.toString()));

        // Verify user exists in database with correct UUID
        User user = userRepository.findByUuid(userUuid).orElseThrow();
        assertThat(user.getUsername()).isEqualTo("uuid_operations_user");
        assertThat(user.getEmail()).isEqualTo("uuid.operations@test.com");
    }
} 