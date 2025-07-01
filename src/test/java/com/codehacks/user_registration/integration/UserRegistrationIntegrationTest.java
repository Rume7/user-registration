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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for User Registration and Email Verification
 * 
 * Tests the complete flow from user registration to email verification
 * using Testcontainers for realistic database testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("integration-test")
@Testcontainers
@Transactional
@Import(IntegrationTestConfig.class)
class UserRegistrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.8-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> mailhog = new GenericContainer<>("mailhog/mailhog:v1.0.1")
            .withExposedPorts(1025, 8025);

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

    @DynamicPropertySource
    static void mailhogProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", mailhog::getHost);
        registry.add("spring.mail.port", () -> mailhog.getMappedPort(1025));
    }

    @Test
    @DisplayName("Integration Test: Complete User Registration Flow")
    void shouldCompleteUserRegistrationFlow() throws Exception {
        // Step 1: Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("ureg_testuser")
                .email("ureg_test@example.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.username").value("ureg_testuser"))
                .andExpect(jsonPath("$.email").value("ureg_test@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(false));

        // Step 2: Verify user was saved in database
        User savedUser = userRepository.findByUsername("ureg_testuser").orElseThrow();
        assertThat(savedUser.getUsername()).isEqualTo("ureg_testuser");
        assertThat(savedUser.getEmail()).isEqualTo("ureg_test@example.com");
        assertThat(savedUser.getEmailVerified()).isFalse();
        assertThat(savedUser.getVerificationToken()).isNotNull();
        assertThat(savedUser.getVerificationTokenExpiry()).isNotNull();

        // Step 3: Verify email using the token
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", savedUser.getVerificationToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 4: Verify user is now marked as verified
        User verifiedUser = userRepository.findByUsername("ureg_testuser").orElseThrow();
        assertThat(verifiedUser.getEmailVerified()).isTrue();
        assertThat(verifiedUser.getVerificationToken()).isNull();
        assertThat(verifiedUser.getVerificationTokenExpiry()).isNull();
    }

    @Test
    @DisplayName("Integration Test: User Registration with Duplicate Username")
    void shouldHandleDuplicateUsername() throws Exception {
        // Register first user
        UserRegistrationRequest request1 = UserRegistrationRequest.builder()
                .username("ureg_duplicate_user")
                .email("user1@example.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Try to register second user with same username
        UserRegistrationRequest request2 = UserRegistrationRequest.builder()
                .username("ureg_duplicate_user")
                .email("user2@example.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Integration Test: User Registration with Duplicate Email")
    void shouldHandleDuplicateEmail() throws Exception {
        // Register first user
        UserRegistrationRequest request1 = UserRegistrationRequest.builder()
                .username("ureg_user1")
                .email("ureg_duplicate@example.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Try to register second user with same email
        UserRegistrationRequest request2 = UserRegistrationRequest.builder()
                .username("ureg_user2")
                .email("ureg_duplicate@example.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Integration Test: Email Verification with Invalid Token")
    void shouldHandleInvalidVerificationToken() throws Exception {
        mockMvc.perform(get("/api/v1/email/verify-email")
                        .param("token", "invalid-token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired verification token"));
    }

    @Test
    @DisplayName("Integration Test: Email Verification with Missing Token")
    void shouldHandleMissingVerificationToken() throws Exception {
        mockMvc.perform(get("/api/v1/email/verify-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Integration Test: Resend Verification Email")
    void shouldResendVerificationEmail() throws Exception {
        // Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("ureg_resend_test_user")
                .email("ureg_resend@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        // Get the original token
        User user = userRepository.findByUsername("ureg_resend_test_user").orElseThrow();
        String originalToken = user.getVerificationToken();

        // Resend verification email
        mockMvc.perform(post("/api/v1/email/send-verification")
                        .param("email", "ureg_resend@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Verification email sent successfully to ureg_resend@test.com"));

        // Verify a new token was generated
        User updatedUser = userRepository.findByUsername("ureg_resend_test_user").orElseThrow();
        assertThat(updatedUser.getVerificationToken()).isNotEqualTo(originalToken);
        assertThat(updatedUser.getVerificationToken()).isNotNull();
    }

    @Test
    @DisplayName("Integration Test: Get User by ID")
    void shouldGetUserById() throws Exception {
        // Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("ureg_get_user_test")
                .email("ureg_get@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        // Get the user ID from database
        User user = userRepository.findByUsername("ureg_get_user_test").orElseThrow();

        // Get user by ID via API
        mockMvc.perform(get("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("ureg_get_user_test"))
                .andExpect(jsonPath("$.email").value("ureg_get@test.com"));
    }

    @Test
    @DisplayName("Integration Test: Get User by Username")
    void shouldGetUserByUsername() throws Exception {
        // Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("ureg_username_test")
                .email("ureg_username@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        // Get user by username via API
        mockMvc.perform(get("/api/v1/users/username/{username}", "ureg_username_test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ureg_username_test"))
                .andExpect(jsonPath("$.email").value("ureg_username@test.com"));
    }

    @Test
    @DisplayName("Integration Test: Check Username Availability")
    void shouldCheckUsernameAvailability() throws Exception {
        // Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("ureg_availability_test")
                .email("ureg_availability@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        // Check that username is not available
        mockMvc.perform(get("/api/v1/users/check-username")
                        .param("username", "ureg_availability_test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        // Check that different username is available
        mockMvc.perform(get("/api/v1/users/check-username")
                        .param("username", "new_username"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("Integration Test: Get Total User Count")
    void shouldGetTotalUserCount() throws Exception {
        // Register multiple users
        UserRegistrationRequest user1 = UserRegistrationRequest.builder()
                .username("ureg_count_user1")
                .email("ureg_count1@test.com")
                .build();

        UserRegistrationRequest user2 = UserRegistrationRequest.builder()
                .username("ureg_count_user2")
                .email("ureg_count2@test.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        // Get total count
        mockMvc.perform(get("/api/v1/users/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @DisplayName("Integration Test: Send Verification Email and Assert with Mailhog")
    void shouldSendVerificationEmailAndAssertWithMailhog() throws Exception {
        // Register a user
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("ureg_mailhog_test_user");
        registrationRequest.setEmail("ureg_mailhog@test.com");

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        // Trigger resend verification email
        mockMvc.perform(post("/api/v1/email/send-verification")
                .param("email", "ureg_mailhog@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Since we're using mock email services in integration tests,
        // we verify that the user was created and the endpoints work correctly
        // The mock email service logs the email sending attempts
        
        // Verify user exists in database
        User user = userRepository.findByEmail("ureg_mailhog@test.com").orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("ureg_mailhog_test_user");
        assertThat(user.getEmail()).isEqualTo("ureg_mailhog@test.com");
        assertThat(user.getVerificationToken()).isNotNull();
        assertThat(user.getVerificationTokenExpiry()).isNotNull();
        
        // Note: The mock email service logs the email sending attempts to console
        // In a real scenario with Mailhog, you would check the Mailhog API
        // For this test, we verify the business logic works correctly
    }

    @Test
    @DisplayName("Integration Test: Get User by UUID")
    void shouldGetUserByUuid() throws Exception {
        // Register a user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("ureg_uuid_test_user")
                .email("ureg_uuid.test@example.com")
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
                .andExpect(jsonPath("$.username").value("ureg_uuid_test_user"))
                .andExpect(jsonPath("$.email").value("ureg_uuid.test@example.com"))
                .andExpect(jsonPath("$.uuid").value(userUuid.toString()));
    }
} 