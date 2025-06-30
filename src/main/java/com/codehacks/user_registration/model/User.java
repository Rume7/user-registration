package com.codehacks.user_registration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Entity - Represents the users table in PostgreSQL database
 *
 * This class demonstrates:
 * 1. JPA Entity mapping - how Java objects map to database tables
 * 2. Lombok usage - reducing boilerplate code
 * 3. Database constraints and relationships
 *
 * JPA Annotations Explained:
 * - @Entity: Marks this class as a JPA entity (database table)
 * - @Table: Specifies the table name and constraints
 * - @Id: Marks the primary key field
 * - @GeneratedValue: Specifies how the primary key is generated
 * - @Column: Specifies column properties and constraints
 *
 * Lombok Annotations Explained:
 * - @Data: Generates getters, setters, toString, equals, and hashCode
 * - @NoArgsConstructor: Generates a no-argument constructor (required by JPA)
 * - @AllArgsConstructor: Generates a constructor with all fields
 * - @Builder: Enables the builder pattern for object creation
 */
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    /**
     * JPA Lifecycle callback - automatically sets createdAt before persisting
     * This ensures createdAt is always set without manual intervention
     *
     * @PrePersist is called before the entity is saved to the database
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
