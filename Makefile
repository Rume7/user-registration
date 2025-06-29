# User Registration Application Makefile

# Variables
APP_NAME = user-registration
DOCKER_IMAGE = user-registration
DOCKER_TAG = latest
CONTAINER_NAME = user-registration-app

# Default target
.PHONY: help
help:
	@echo "Available targets:"
	@echo "  build     - Build the application"
	@echo "  test      - Run tests"
	@echo "  run       - Run the application"
	@echo "  dev       - Run in development mode with H2 database"
	@echo "  clean     - Clean build artifacts"
	@echo "  docker-build - Build Docker image"
	@echo "  docker-run   - Run Docker container"
	@echo "  docker-stop  - Stop Docker container"
	@echo "  docker-clean - Remove Docker container and image"
	@echo "  compose-up   - Start with Docker Compose"
	@echo "  compose-down - Stop Docker Compose"
	@echo "  logs        - Show application logs"
	@echo "  version     - Show current version"
	@echo "  bump-patch  - Bump patch version (1.0.0 -> 1.0.1)"
	@echo "  bump-minor  - Bump minor version (1.0.0 -> 1.1.0)"
	@echo "  bump-major  - Bump major version (1.0.0 -> 2.0.0)"
	@echo ""
	@echo "Email Testing Commands:"
	@echo "  mailhog-up    - Start MailHog for email testing"
	@echo "  mailhog-down  - Stop MailHog"
	@echo "  test-email    - Run email functionality tests"
	@echo "  mailhog-ui    - Open MailHog web interface"

# Build the application
.PHONY: build
build:
	mvn clean compile

# Run tests
.PHONY: test
test:
	mvn test

# Run the application
.PHONY: run
run:
	mvn spring-boot:run

# Run in development mode with H2 database
.PHONY: dev
dev:
	SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run

# Clean build artifacts
.PHONY: clean
clean:
	mvn clean

# Build Docker image
.PHONY: docker-build
docker-build:
	docker build -t $(DOCKER_IMAGE):$(DOCKER_TAG) .

# Run Docker container
.PHONY: docker-run
docker-run:
	docker run -d \
		--name $(CONTAINER_NAME) \
		-p 8080:8080 \
		-e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/userdb \
		-e SPRING_DATASOURCE_USERNAME=user \
		-e SPRING_DATASOURCE_PASSWORD=password \
		$(DOCKER_IMAGE):$(DOCKER_TAG)

# Stop Docker container
.PHONY: docker-stop
docker-stop:
	docker stop $(CONTAINER_NAME) || true

# Remove Docker container and image
.PHONY: docker-clean
docker-clean:
	docker stop $(CONTAINER_NAME) || true
	docker rm $(CONTAINER_NAME) || true
	docker rmi $(DOCKER_IMAGE):$(DOCKER_TAG) || true

# Start with Docker Compose
.PHONY: compose-up
compose-up:
	docker-compose up -d

# Stop Docker Compose
.PHONY: compose-down
compose-down:
	docker-compose down

# Show application logs
.PHONY: logs
logs:
	docker-compose logs -f app

# Show current version
.PHONY: version
version:
	@echo "Current version: $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"

# Bump patch version (1.0.0 -> 1.0.1)
.PHONY: bump-patch
bump-patch:
	./scripts/bump-version.sh --patch

# Bump minor version (1.0.0 -> 1.1.0)
.PHONY: bump-minor
bump-minor:
	./scripts/bump-version.sh --minor

# Bump major version (1.0.0 -> 2.0.0)
.PHONY: bump-major
bump-major:
	./scripts/bump-version.sh --major

# Development setup
.PHONY: dev-setup
dev-setup:
	@echo "Setting up development environment..."
	@echo "1. Starting PostgreSQL..."
	docker run -d \
		--name postgres-dev \
		-e POSTGRES_DB=userdb \
		-e POSTGRES_USER=user \
		-e POSTGRES_PASSWORD=password \
		-p 5432:5432 \
		postgres:16.8-alpine
	@echo "2. Waiting for database to be ready..."
	@sleep 10
	@echo "3. Database is ready!"
	@echo "4. You can now run: make run"

# Clean development environment
.PHONY: dev-clean
dev-clean:
	docker stop postgres-dev || true
	docker rm postgres-dev || true

# Full clean (everything)
.PHONY: full-clean
full-clean: clean docker-clean dev-clean
	@echo "All build artifacts and containers cleaned"

# Email Testing Commands

# Start MailHog for email testing
.PHONY: mailhog-up
mailhog-up:
	@echo "Starting MailHog for email testing..."
	docker-compose -f docker-compose.dev.yml up -d mailhog
	@echo "MailHog is running!"
	@echo "SMTP Server: localhost:1025"
	@echo "Web UI: http://localhost:8025"

# Stop MailHog
.PHONY: mailhog-down
mailhog-down:
	@echo "Stopping MailHog..."
	docker-compose -f docker-compose.dev.yml down
	@echo "MailHog stopped"

# Run email functionality tests
.PHONY: test-email
test-email:
	@echo "Running email functionality tests..."
	@chmod +x scripts/test-email.sh
	./scripts/test-email.sh

# Open MailHog web interface
.PHONY: mailhog-ui
mailhog-ui:
	@echo "Opening MailHog web interface..."
	@if command -v open > /dev/null; then \
		open http://localhost:8025; \
	elif command -v xdg-open > /dev/null; then \
		xdg-open http://localhost:8025; \
	else \
		echo "Please open http://localhost:8025 in your browser"; \
	fi

# Complete email testing setup
.PHONY: email-test-setup
email-test-setup: mailhog-up
	@echo "Email testing environment is ready!"
	@echo "1. MailHog is running on localhost:1025"
	@echo "2. Web UI available at http://localhost:8025"
	@echo "3. Start the application: make dev"
	@echo "4. Run email tests: make test-email" 