package com.codehacks.user_registration.service;

import com.codehacks.user_registration.model.User;
import com.codehacks.user_registration.exception.UserRegistrationException;
import com.codehacks.user_registration.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 *
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User savedUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doNothing().when(emailVerificationService).sendVerificationEmail(any(User.class));

        // When
        User result = userService.registerUser("testuser", "test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(emailVerificationService).sendVerificationEmail(any(User.class));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("Should throw exception when username is null")
    void shouldThrowExceptionWhenUsernameIsNull() {
        // When & Then
        assertThatThrownBy(() -> userService.registerUser(null, "test@example.com"))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Username cannot be empty or null");
    }

    @Test
    @DisplayName("Should throw exception when username is empty")
    void shouldThrowExceptionWhenUsernameIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> userService.registerUser("", "test@example.com"))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Username cannot be empty or null");
    }

    @Test
    @DisplayName("Should throw exception when username is too short")
    void shouldThrowExceptionWhenUsernameIsTooShort() {
        // When & Then
        assertThatThrownBy(() -> userService.registerUser("ab", "test@example.com"))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Username must be at least 3 characters long");
    }

    @Test
    @DisplayName("Should throw exception when username is too long")
    void shouldThrowExceptionWhenUsernameIsTooLong() {
        // Given
        String longUsername = "a".repeat(31);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(longUsername, "test@example.com"))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Username cannot exceed 30 characters");
    }

    @Test
    @DisplayName("Should throw exception when username contains invalid characters")
    void shouldThrowExceptionWhenUsernameContainsInvalidCharacters() {
        // When & Then
        assertThatThrownBy(() -> userService.registerUser("test-user!", "test@example.com"))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Username can only contain letters, numbers, and underscores");
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void shouldThrowExceptionWhenEmailIsNull() {
        // When & Then
        assertThatThrownBy(() -> userService.registerUser("testuser", null))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Email cannot be empty or null");
    }

    @Test
    @DisplayName("Should throw exception when email is empty")
    void shouldThrowExceptionWhenEmailIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> userService.registerUser("testuser", ""))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Email cannot be empty or null");
    }

    @Test
    @DisplayName("Should throw exception when email is invalid")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        // When & Then
        assertThatThrownBy(() -> userService.registerUser("testuser", "invalid-email"))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Invalid email format");
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser("existinguser", "test@example.com"))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Username 'existinguser' is already taken");

        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser("testuser", "existing@example.com"))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Email 'existing@example.com' is already registered");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void shouldFindUserByIdSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));

        // When
        User result = userService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when user ID is null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // When & Then
        assertThatThrownBy(() -> userService.findById(null))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("User ID cannot be null nor negative");
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("User not found with ID: 999");

        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("Should find user by username successfully")
    void shouldFindUserByUsernameSuccessfully() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(savedUser));

        // When
        User result = userService.findByUsername("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should throw exception when username is null for find")
    void shouldThrowExceptionWhenUsernameIsNullForFind() {
        // When & Then
        assertThatThrownBy(() -> userService.findByUsername(null))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Username cannot be empty or null");
    }

    @Test
    @DisplayName("Should throw exception when user not found by username")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findByUsername("nonexistent"))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("User not found with username: nonexistent");

        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Should return true when username is available")
    void shouldReturnTrueWhenUsernameIsAvailable() {
        // Given
        when(userRepository.existsByUsername("availableuser")).thenReturn(false);

        // When
        boolean result = userService.isUsernameAvailable("availableuser");

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByUsername("availableuser");
    }

    @Test
    @DisplayName("Should return false when username is not available")
    void shouldReturnFalseWhenUsernameIsNotAvailable() {
        // Given
        when(userRepository.existsByUsername("takenuser")).thenReturn(true);

        // When
        boolean result = userService.isUsernameAvailable("takenuser");

        // Then
        assertThat(result).isFalse();
        verify(userRepository).existsByUsername("takenuser");
    }

    @Test
    @DisplayName("Should throw exception when checking username availability with null")
    void shouldThrowExceptionWhenCheckingUsernameAvailabilityWithNull() {
        // When & Then
        assertThatThrownBy(() -> userService.isUsernameAvailable(null))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Username cannot be empty or null");
    }

    @Test
    @DisplayName("Should return true when email is available")
    void shouldReturnTrueWhenEmailIsAvailable() {
        // Given
        when(userRepository.existsByEmail("available@example.com")).thenReturn(false);

        // When
        boolean result = userService.isEmailAvailable("available@example.com");

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail("available@example.com");
    }

    @Test
    @DisplayName("Should return false when email is not available")
    void shouldReturnFalseWhenEmailIsNotAvailable() {
        // Given
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        // When
        boolean result = userService.isEmailAvailable("taken@example.com");

        // Then
        assertThat(result).isFalse();
        verify(userRepository).existsByEmail("taken@example.com");
    }

    @Test
    @DisplayName("Should throw exception when checking email availability with null")
    void shouldThrowExceptionWhenCheckingEmailAvailabilityWithNull() {
        // When & Then
        assertThatThrownBy(() -> userService.isEmailAvailable(null))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Email cannot be empty or null");
    }

    @Test
    @DisplayName("Should return correct user count")
    void shouldReturnCorrectUserCount() {
        // Given
        when(userRepository.count()).thenReturn(42L);

        // When
        long result = userService.getUserCount();

        // Then
        assertThat(result).isEqualTo(42L);
        verify(userRepository).count();
    }
} 