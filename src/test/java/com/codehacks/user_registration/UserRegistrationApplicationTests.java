package com.codehacks.user_registration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for User Registration Application
 * 
 * This test class demonstrates:
 * 1. Spring Boot test configuration
 * 2. Profile-based configuration for testing
 * 3. Context loading verification
 * 4. Integration test setup
 * 5. REST API testing
 * 
 * Test Configuration:
 * - Uses 'test' profile to load application-test.yml
 * - Uses H2 in-memory database for fast, isolated testing
 * - Disables OpenAPI for faster startup
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserRegistrationApplicationTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@LocalServerPort
	private int port;

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

	/**
	 * Test that the application starts and responds to health check
	 */
	@Test
	void healthEndpointShouldReturnOk() {
		// Given
		String url = "http://localhost:" + port + "/actuator/health";

		// When
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("UP");
	}

	/**
	 * Test that the application info endpoint is accessible
	 */
	@Test
	void infoEndpointShouldReturnOk() {
		// Given
		String url = "http://localhost:" + port + "/actuator/info";

		// When
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	/**
	 * Test that the application responds to root endpoint
	 */
	@Test
	void rootEndpointShouldReturnOk() {
		// Given
		String url = "http://localhost:" + port + "/";

		// When
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}
