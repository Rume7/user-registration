# Makefile for User Registration Application
# Provides convenient commands for development and deployment

.PHONY: help build test run docker-build docker-run docker-stop docker-logs clean

# Default target
help:
	@echo "Available commands:"
	@echo "  build        - Build the application with Maven"
	@echo "  test         - Run tests"
	@echo "  run          - Run the application locally"
	@echo "  docker-build - Build Docker image"
	@echo "  docker-run   - Start application with Docker Compose"
	@echo "  docker-stop  - Stop Docker containers"
	@echo "  docker-logs  - Show Docker logs"
	@echo "  clean        - Clean build artifacts"
	@echo "  prod-run     - Start production environment"
	@echo "  backup       - Create database backup"

# Build the application
build:
	@echo "Building application..."
	./mvnw clean compile

# Run tests
test:
	@echo "Running tests..."
	./mvnw test

# Run the application locally
run:
	@echo "Starting application locally..."
	./mvnw spring-boot:run

# Build Docker image
docker-build:
	@echo "Building Docker image..."
	docker-compose build

# Start application with Docker Compose
docker-run:
	@echo "Starting application with Docker Compose..."
	docker-compose up -d

# Stop Docker containers
docker-stop:
	@echo "Stopping Docker containers..."
	docker-compose down

# Show Docker logs
docker-logs:
	@echo "Showing Docker logs..."
	docker-compose logs -f

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	./mvnw clean
	docker system prune -f

# Start production environment
prod-run:
	@echo "Starting production environment..."
	docker-compose -f docker-compose.prod.yml up -d

# Create database backup
backup:
	@echo "Creating database backup..."
	docker-compose exec db pg_dump -U postgres userdb > backup/backup_$(shell date +%Y%m%d_%H%M%S).sql

# Check application health
health:
	@echo "Checking application health..."
	@curl -f http://localhost:8080/actuator/health || echo "Application is not healthy"

# Show application info
info:
	@echo "Application Information:"
	@echo "  API Base URL: http://localhost:8080"
	@echo "  Swagger UI: http://localhost:8080/swagger-ui.html"
	@echo "  Health Check: http://localhost:8080/actuator/health"
	@echo "  Database: localhost:5432" 