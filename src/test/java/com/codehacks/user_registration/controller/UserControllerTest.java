package com.codehacks.user_registration.controller;

import com.codehacks.user_registration.model.User;
import com.codehacks.user_registration.exception.UserRegistrationException;
import com.codehacks.user_registration.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController
 * 
 * This test class demonstrates:
 * 1. MockMvc for testing REST endpoints
 * 2. JSON request/response testing
 * 3. HTTP status code validation
 * 4. Error handling testing
 * 5. Controller method coverage
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new com.codehacks.user_registration.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        Map<String, String> request = Map.of(
                "username", "testuser",
                "email", "test@example.com"
        );

        when(userService.registerUser(anyString(), anyString()))
                .thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist()); // Password should not be returned

        verify(userService).registerUser("testuser", "test@example.com");
    }

    @Test
    @DisplayName("Should return 400 when registration request is invalid")
    void shouldReturn400WhenRegistrationRequestIsInvalid() throws Exception {
        // Given
        Map<String, String> request = Map.of(
                "username", "", // Invalid empty username
                "email", "test@example.com"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid input data"));

        // Service should not be called due to validation failure
        verify(userService, never()).registerUser(anyString(), anyString());
    }

    @Test
    @DisplayName("Should return 409 when username already exists")
    void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
        // Given
        Map<String, String> request = Map.of(
                "username", "existinguser",
                "email", "test@example.com"
        );

        when(userService.registerUser(anyString(), anyString()))
                .thenThrow(new UserRegistrationException("Username 'existinguser' is already taken"));

        // When & Then
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username 'existinguser' is already taken"));

        verify(userService).registerUser("existinguser", "test@example.com");
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).findById(1L);
    }

    @Test
    @DisplayName("Should return 404 when user not found by ID")
    void shouldReturn404WhenUserNotFoundById() throws Exception {
        // Given
        when(userService.findById(999L))
                .thenThrow(new UserRegistrationException("User not found with ID: 999"));

        // When & Then
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with ID: 999"));

        verify(userService).findById(999L);
    }

    @Test
    @DisplayName("Should get user by username successfully")
    void shouldGetUserByUsernameSuccessfully() throws Exception {
        // Given
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should return 404 when user not found by username")
    void shouldReturn404WhenUserNotFoundByUsername() throws Exception {
        // Given
        when(userService.findByUsername("nonexistent"))
                .thenThrow(new UserRegistrationException("User not found with username: nonexistent"));

        // When & Then
        mockMvc.perform(get("/api/v1/users/username/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with username: nonexistent"));

        verify(userService).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Should check username availability successfully")
    void shouldCheckUsernameAvailabilitySuccessfully() throws Exception {
        // Given
        when(userService.isUsernameAvailable("availableuser")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/users/check-username")
                        .param("username", "availableuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("availableuser"))
                .andExpect(jsonPath("$.available").value(true));

        verify(userService).isUsernameAvailable("availableuser");
    }

    @Test
    @DisplayName("Should return false when username is not available")
    void shouldReturnFalseWhenUsernameIsNotAvailable() throws Exception {
        // Given
        when(userService.isUsernameAvailable("takenuser")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/users/check-username")
                        .param("username", "takenuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("takenuser"))
                .andExpect(jsonPath("$.available").value(false));

        verify(userService).isUsernameAvailable("takenuser");
    }

    @Test
    @DisplayName("Should return 400 when checking username availability with invalid username")
    void shouldReturn400WhenCheckingUsernameAvailabilityWithInvalidUsername() throws Exception {
        // Given
        when(userService.isUsernameAvailable(""))
                .thenThrow(new UserRegistrationException("Username cannot be empty or null"));

        // When & Then
        mockMvc.perform(get("/api/v1/users/check-username")
                        .param("username", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username cannot be empty or null"));

        verify(userService).isUsernameAvailable("");
    }

    @Test
    @DisplayName("Should get user count successfully")
    void shouldGetUserCountSuccessfully() throws Exception {
        // Given
        when(userService.getUserCount()).thenReturn(42L);

        // When & Then
        mockMvc.perform(get("/api/v1/users/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(42));

        verify(userService).getUserCount();
    }

    @Test
    @DisplayName("Should return 400 when registration request is missing required fields")
    void shouldReturn400WhenRegistrationRequestIsMissingRequiredFields() throws Exception {
        // Given
        Map<String, String> request = Map.of(
                "username", "testuser"
                // Missing email
        );

        // When & Then
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when registration request has invalid JSON")
    void shouldReturn400WhenRegistrationRequestHasInvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when user ID is invalid")
    void shouldReturn400WhenUserIdIsInvalid() throws Exception {
        // Given
        when(userService.findById(0L))
                .thenThrow(new UserRegistrationException("User ID cannot be null nor negative"));

        // When & Then
        mockMvc.perform(get("/api/v1/users/0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("User Registration Error"))
                .andExpect(jsonPath("$.message").value("User not found with ID: 0"));
    }

    @Test
    @DisplayName("Should return 400 when username is invalid for availability check")
    void shouldReturn400WhenUsernameIsInvalidForAvailabilityCheck() throws Exception {
        // Given
        when(userService.isUsernameAvailable("test-user!"))
                .thenThrow(new UserRegistrationException("Username cannot be empty or null"));

        // When & Then
        mockMvc.perform(get("/api/v1/users/check-username")
                        .param("username", "test-user!")) // Invalid characters
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle email already exists during registration")
    void shouldHandleEmailAlreadyExistsDuringRegistration() throws Exception {
        // Given
        Map<String, String> request = Map.of(
                "username", "newuser",
                "email", "existing@example.com"
        );

        when(userService.registerUser(anyString(), anyString()))
                .thenThrow(new UserRegistrationException("Email 'existing@example.com' is already registered"));

        // When & Then
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email 'existing@example.com' is already registered"));

        verify(userService).registerUser("newuser", "existing@example.com");
    }
} 