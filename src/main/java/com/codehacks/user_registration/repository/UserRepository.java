package com.codehacks.user_registration.repository;

import com.codehacks.user_registration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - Data Access Layer for User entity
 *
 * This interface demonstrates:
 * 1. Spring Data JPA repository pattern
 * 2. Automatic query generation from method names
 * 3. Custom query methods for business logic
 *
 * Spring Data JPA Concepts:
 * - JpaRepository provides CRUD operations out of the box
 * - Method names are automatically converted to SQL queries
 * - No need to write implementation - Spring generates it at runtime
 *
 * Benefits of Repository Pattern:
 * - Encapsulates data access logic
 * - Provides a clean abstraction over database operations
 * - Easy to test with mocks
 * - Supports different data sources without changing business logic
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Check if a user exists with the given username
     *
     * Method name convention: existsBy + PropertyName
     * Spring Data JPA automatically generates:
     * SELECT COUNT(u) > 0 FROM User u WHERE u.username = ?1
     *
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if a user exists with the given email
     *
     * Similar to existsByUsername, this generates:
     * SELECT COUNT(u) > 0 FROM User u WHERE u.email = ?1
     *
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find a user by username
     *
     * Method name convention: findBy + PropertyName
     * Spring Data JPA automatically generates:
     * SELECT u FROM User u WHERE u.username = ?1
     *
     * Returns Optional to handle cases where user might not exist
     * This is a modern Java approach to avoid NullPointerException
     *
     * @param username the username to search for
     * @return Optional containing User if found, empty Optional otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email
     *
     * Similar to findByUsername, returns Optional<User>
     *
     * @param email the email to search for
     * @return Optional containing User if found, empty Optional otherwise
     */
    Optional<User> findByEmail(String email);

    // Additional methods you might need:

    /**
     * Find users by username containing a specific string (case-insensitive)
     * Useful for search functionality
     *
     * Generates: SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', ?1, '%'))
     */
    // List<User> findByUsernameContainingIgnoreCase(String username);

    /**
     * Count users created after a specific date
     * Useful for analytics
     *
     * Generates: SELECT COUNT(u) FROM User u WHERE u.createdAt > ?1
     */
    // long countByCreatedAtAfter(LocalDateTime date);
}