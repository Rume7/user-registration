# Event-Driven User Registration Application

A Spring Boot application demonstrating event-driven architecture with user registration functionality. This application showcases how to build loosely coupled, scalable systems using Spring's internal event system.

## 🚀 Features

- **User Registration**: RESTful API for user registration with validation
- **Event-Driven Architecture**: Uses Spring's `ApplicationEventPublisher` for loose coupling
- **Database Persistence**: PostgreSQL with JPA/Hibernate
- **API Documentation**: OpenAPI/Swagger UI for interactive API testing
- **Containerized**: Docker and Docker Compose for easy deployment
- **Input Validation**: Comprehensive validation using Bean Validation
- **Logging**: Structured logging with configurable levels

## 🏗️ Architecture

### Event-Driven Components

1. **UserService**: Publishes `UserRegisteredEvent` when a user is registered
2. **EmailNotificationListener**: Listens for registration events and simulates email sending
3. **WelcomeBonusListener**: Listens for registration events and simulates bonus granting

### Key Benefits

- **Loose Coupling**: Services don't directly depend on each other
- **Extensibility**: Easy to add new listeners without modifying existing code
- **Testability**: Each component can be tested in isolation
- **Scalability**: Listeners can be moved to separate services in the future

## 🛠️ Technology Stack

- **Java 17**: Modern Java features and performance
- **Spring Boot 3.3.13**: Latest stable version with Spring Framework 6
- **Spring Data JPA**: Database persistence with Hibernate
- **PostgreSQL**: Reliable, open-source database
- **Lombok**: Reduces boilerplate code
- **SpringDoc OpenAPI**: API documentation and testing
- **Docker**: Containerization for consistent deployment
- **Docker Compose**: Multi-service orchestration

## 📁 Project Structure

```
src/main/java/com/codehacks/user_registration/
├── UserRegistrationApplication.java    # Main application class
├── controller/
│   └── UserController.java            # REST API endpoints
├── service/
│   └── UserService.java               # Business logic and event publishing
├── repository/
│   └── UserRepository.java            # Data access layer
├── model/
│   └── User.java                      # JPA entity
├── dto/
│   ├── UserRegistrationRequest.java   # Request DTO with validation
│   └── UserResponse.java              # Response DTO
├── event/
│   └── UserRegisteredEvent.java       # Custom application event
└── listener/
    ├── EmailNotificationListener.java # Email notification handler
    └── WelcomeBonusListener.java      # Welcome bonus handler
```

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose installed
- Java 17+ (for local development)
- Maven 3.6+ (for local development)

### Running with Docker Compose

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd user-registration
   ```

2. **Start the application**
   ```bash
   docker-compose up --build
   ```

3. **Access the application**
   - API Base URL: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Database: localhost:5432 (userdb)

4. **Stop the application**
   ```bash
   docker-compose down
   ```

### Local Development

1. **Set up PostgreSQL**
   ```bash
   # Using Docker for database only
   docker run --name postgres-db -e POSTGRES_DB=userdb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:16.8-alpine
   ```

2. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

## 📚 API Documentation

### Register User

**POST** `/api/v1/users/register`

```json
{
  "username": "john_doe",
  "email": "john.doe@example.com"
}
```

**Response (201 Created)**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john.doe@example.com",
  "createdAt": "2024-01-15T10:30:00"
}
```

### Get User by ID

**GET** `/api/v1/users/{id}`

**Response (200 OK)**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john.doe@example.com",
  "createdAt": "2024-01-15T10:30:00"
}
```

### Check Username Availability

**GET** `/api/v1/users/check-username?username=john_doe`

**Response (200 OK)**
```json
true
```

### Get User Count

**GET** `/api/v1/users/count`

**Response (200 OK)**
```json
42
```

## 🔧 Configuration

### Environment Variables

The application can be configured using environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_DB` | `userdb` | Database name |
| `POSTGRES_USER` | `postgres` | Database username |
| `POSTGRES_PASSWORD` | `password` | Database password |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Database schema strategy |
| `SPRING_JPA_SHOW_SQL` | `false` | Show SQL queries in logs |
| `SERVER_PORT` | `8080` | Application port |

### Application Properties

Key configuration in `application.yml`:

- **Database**: PostgreSQL with connection pooling
- **JPA**: Hibernate with PostgreSQL dialect
- **Logging**: Configurable log levels
- **OpenAPI**: API documentation settings

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

### API Testing

1. **Using Swagger UI**
   - Open http://localhost:8080/swagger-ui.html
   - Interactive API documentation and testing

2. **Using curl**
   ```bash
   # Register a user
   curl -X POST http://localhost:8080/api/v1/users/register \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","email":"test@example.com"}'
   ```

## 📊 Monitoring

### Health Checks

- **Application Health**: http://localhost:8080/actuator/health
- **Database Health**: Monitored via Docker Compose health checks

### Logging

The application uses structured logging with configurable levels:

- **Application Logs**: `com.codehacks.user_registration`
- **Spring Framework**: `org.springframework`
- **Hibernate SQL**: `org.hibernate.SQL`

## 🔒 Security Considerations

- **Input Validation**: All inputs are validated using Bean Validation
- **SQL Injection Protection**: Uses JPA/Hibernate with parameterized queries
- **Container Security**: Application runs as non-root user in Docker
- **Environment Variables**: Sensitive data stored in environment variables

## 🚀 Deployment

### Production Deployment

1. **Build the image**
   ```bash
   docker build -t user-registration:latest .
   ```

2. **Deploy with Docker Compose**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

### Kubernetes Deployment

The application can be deployed to Kubernetes using the provided manifests in the `k8s/` directory.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:

- Create an issue in the repository
- Check the [documentation](docs/)
- Review the [API documentation](http://localhost:8080/swagger-ui.html)

---

**Happy Coding! 🎉** 