package com.codehacks.user_registration.repository;

import com.codehacks.user_registration.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository
 *
 * Tests cover:
 * - Basic CRUD operations
 * - Email verification queries
 * - Username and email uniqueness
 * - Verification token queries
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getEmailVerified()).isFalse();
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {
        // Given
        User savedUser = userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should return empty when username not found")
    void shouldReturnEmptyWhenUsernameNotFound() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        User savedUser = userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void shouldReturnEmptyWhenEmailNotFound() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find user by verification token")
    void shouldFindUserByVerificationToken() {
        // Given
        testUser.setVerificationToken("test-token-123");
        testUser.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        User savedUser = userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByVerificationToken("test-token-123");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getVerificationToken()).isEqualTo("test-token-123");
    }

    @Test
    @DisplayName("Should return empty when verification token not found")
    void shouldReturnEmptyWhenVerificationTokenNotFound() {
        // When
        Optional<User> foundUser = userRepository.findByVerificationToken("invalid-token");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should check if username exists")
    void shouldCheckIfUsernameExists() {
        // Given
        userRepository.save(testUser);

        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        // Given
        userRepository.save(testUser);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should update user verification status")
    void shouldUpdateUserVerificationStatus() {
        // Given
        User savedUser = userRepository.save(testUser);
        savedUser.setEmailVerified(true);
        savedUser.setVerificationToken(null);
        savedUser.setVerificationTokenExpiry(null);

        // When
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertThat(updatedUser.getEmailVerified()).isTrue();
        assertThat(updatedUser.getVerificationToken()).isNull();
        assertThat(updatedUser.getVerificationTokenExpiry()).isNull();
    }

    @Test
    @DisplayName("Should find user with verification token and expiry")
    void shouldFindUserWithVerificationTokenAndExpiry() {
        // Given
        LocalDateTime expiry = LocalDateTime.now().plusHours(24);
        testUser.setVerificationToken("expiry-test-token");
        testUser.setVerificationTokenExpiry(expiry);
        User savedUser = userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByVerificationToken("expiry-test-token");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getVerificationTokenExpiry()).isEqualTo(expiry);
    }

    @Test
    @DisplayName("Should handle null verification token")
    void shouldHandleNullVerificationToken() {
        // Given
        testUser.setVerificationToken(null);
        userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByVerificationToken(null);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getVerificationToken()).isNull();
    }

    @Test
    @DisplayName("Should count total users")
    void shouldCountTotalUsers() {
        // Given
        userRepository.save(testUser);
        User secondUser = User.builder()
                .username("testuser2")
                .email("test2@example.com")
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(secondUser);

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        // Given
        User savedUser = userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should return empty when user ID not found")
    void shouldReturnEmptyWhenUserIdNotFound() {
        // When
        Optional<User> foundUser = userRepository.findById(999L);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should handle case-sensitive username search")
    void shouldHandleCaseSensitiveUsernameSearch() {
        // Given
        userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByUsername("TESTUSER");

        // Then
        assertThat(foundUser).isEmpty(); // Should be case-sensitive
    }

    @Test
    @DisplayName("Should handle case-sensitive email search")
    void shouldHandleCaseSensitiveEmailSearch() {
        // Given
        userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("TEST@EXAMPLE.COM");

        // Then
        assertThat(foundUser).isEmpty(); // Should be case-sensitive
    }

    @Test
    @DisplayName("Should handle empty verification token search")
    void shouldHandleEmptyVerificationTokenSearch() {
        // Given
        testUser.setVerificationToken("");
        userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByVerificationToken("");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getVerificationToken()).isEmpty();
    }
} 