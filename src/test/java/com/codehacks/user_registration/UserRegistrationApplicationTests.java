package com.codehacks.user_registration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for User Registration Application
 * 
 * This test class demonstrates:
 * 1. Spring Boot test configuration
 * 2. Profile-based configuration for testing
 * 3. Context loading verification
 * 4. Integration test setup
 * 
 * Test Configuration:
 * - Uses 'test' profile to load application-test.yml
 * - Uses H2 in-memory database for fast, isolated testing
 * - Disables OpenAPI for faster startup
 */
@SpringBootTest
@ActiveProfiles("test")
class UserRegistrationApplicationTests {

	/**
	 * Test that the Spring application context loads successfully
	 * This verifies that all beans are properly configured and wired
	 */
	@Test
	void contextLoads() {
		// If this test passes, it means:
		// 1. All Spring beans are properly configured
		// 2. Database connection is working
		// 3. Event listeners are registered
		// 4. Controllers are properly mapped
	}
}
