# Multi-stage Dockerfile for Spring Boot User Registration Application
# This Dockerfile demonstrates best practices for containerizing Java applications
# including multi-stage builds for smaller final images and security considerations

# Stage 1: Build Stage
# This stage compiles the Java application and creates the executable JAR
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

# Set working directory for the build
WORKDIR /app

# Copy Maven configuration files first (for better layer caching)
# Maven will cache dependencies if pom.xml hasn't changed
COPY pom.xml .
COPY .mvn .mvn

# Download dependencies (this layer will be cached if pom.xml doesn't change)
# The --no-transfer-progress flag reduces build output noise
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
# -DskipTests skips running tests during build (tests should be run separately)
# -B runs in batch mode (non-interactive)
RUN mvn clean package -DskipTests

# Stage 2: Runtime Stage
# This stage creates the final lightweight runtime image
FROM eclipse-temurin:17-jre-alpine

# Add metadata to the image
LABEL maintainer="User Registration Team"
LABEL description="Event-driven user registration application"
LABEL version="1.0.0"

# Create a non-root user for security
# Running as root in containers is a security risk
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the built JAR from the build stage
# The JAR file contains all dependencies and is self-contained
COPY --from=build /app/target/*.jar app.jar

# Change ownership of the application files to the non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose the port the application runs on
# This is a documentation feature - you still need to map ports in docker-compose
EXPOSE 8080

# Health check to verify the application is running
# This helps container orchestration platforms monitor application health
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for production deployment
# These settings optimize the JVM for containerized environments
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Command to run the application
# The exec form is preferred over shell form for better signal handling
ENTRYPOINT ["java", "-jar", "app.jar"] 