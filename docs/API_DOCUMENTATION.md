# User Registration API Documentation

## Overview

This API provides user registration and email verification functionality with support for both traditional numeric IDs and secure UUIDs. The system uses UUIDs as public identifiers for enhanced security while maintaining backward compatibility with numeric IDs.

## Security Features

### UUID Implementation
- **Public Identifiers**: UUIDs are used as public-facing identifiers in API responses and endpoints
- **Security Benefits**: 
  - Prevents enumeration attacks
  - No sequential ID exposure
  - Globally unique identifiers
  - No information leakage about system size or user count
- **Backward Compatibility**: Traditional numeric IDs are still supported for internal operations

## Base URL
```
http://localhost:9090/api/v1
```

## Authentication
Currently, no authentication is required for these endpoints.

## User Management Endpoints

### 1. Register User

**POST** `/users/register`

Creates a new user account with username and email. Triggers welcome email and email verification.

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john.doe@example.com"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john.doe@example.com",
  "emailVerified": false,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Validation Rules:**
- Username: 3-30 characters, alphanumeric + underscore only
- Email: Valid email format, max 255 characters
- Both username and email must be unique

### 2. Get User by ID

**GET** `/users/{id}`

Retrieves a user by their numeric ID (internal use).

**Response (200 OK):**
```json
{
  "id": 1,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john.doe@example.com",
  "emailVerified": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### 3. Get User by UUID ⭐

**GET** `/users/uuid/{uuid}`

Retrieves a user by their UUID (recommended for public APIs).

**Response (200 OK):**
```json
{
  "id": 1,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john.doe@example.com",
  "emailVerified": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### 4. Get User by Username

**GET** `/users/username/{username}`

Retrieves a user by their username.

### 5. Check Username Availability

**GET** `/users/check-username?username={username}`

Checks if a username is available for registration.

**Response (200 OK):**
```json
{
  "username": "john_doe",
  "available": false
}
```

### 6. Get Total User Count

**GET** `/users/count`

Returns the total number of registered users.

**Response (200 OK):**
```json
{
  "count": 42
}
```

## Email Verification Endpoints

### 1. Send Verification Email

**POST** `/email/send-verification?email={email}`

Sends a verification email to the specified email address.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Verification email sent successfully to john.doe@example.com"
}
```

### 2. Verify Email

**GET** `/email/verify-email?token={token}`

Verifies a user's email address using the verification token from the email.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Email verified successfully for user john_doe",
  "username": "john_doe",
  "email": "john.doe@example.com"
}
```

### 3. Check Verification Status by User ID

**GET** `/email/verification-status/{userId}`

Checks if a user's email is verified by their numeric ID.

**Response (200 OK):**
```json
{
  "verified": true,
  "message": "Email is verified",
  "userId": 1
}
```

### 4. Check Verification Status by Username

**GET** `/email/verification-status/username/{username}`

Checks if a user's email is verified by their username.

**Response (200 OK):**
```json
{
  "verified": true,
  "message": "Email is verified",
  "username": "john_doe"
}
```

### 5. Check Verification Status by UUID ⭐

**GET** `/email/verification-status/uuid/{uuid}`

Checks if a user's email is verified by their UUID (recommended for public APIs).

**Response (200 OK):**
```json
{
  "verified": true,
  "message": "Email is verified",
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

## Error Responses

### 400 Bad Request
```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "Username must be between 3 and 20 characters"
}
```

### 404 Not Found
```json
{
  "status": 404,
  "error": "User Registration Error",
  "message": "User not found with UUID: 550e8400-e29b-41d4-a716-446655440000"
}
```

### 409 Conflict
```json
{
  "status": 409,
  "error": "User Registration Error",
  "message": "Username 'john_doe' is already taken"
}
```

### 422 Unprocessable Entity
```json
{
  "username": "Username must be between 3 and 20 characters",
  "email": "Invalid email format"
}
```

## Best Practices

### Security Recommendations
1. **Use UUIDs for Public APIs**: Always use UUID-based endpoints when exposing user data publicly
2. **Avoid Sequential IDs**: Never expose sequential numeric IDs in public responses
3. **Input Validation**: Always validate and sanitize input data
4. **Rate Limiting**: Implement rate limiting for registration and email endpoints
5. **HTTPS**: Always use HTTPS in production

### API Usage Guidelines
1. **UUID Endpoints**: Use `/users/uuid/{uuid}` and `/email/verification-status/uuid/{uuid}` for public-facing operations
2. **Error Handling**: Always check HTTP status codes and handle errors appropriately
3. **Validation**: Validate all input data before sending requests
4. **Caching**: Consider caching user data by UUID for better performance

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    username VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    verification_token_expiry TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX users_uuid_idx ON users(uuid);
CREATE INDEX users_username_idx ON users(username);
CREATE INDEX users_email_idx ON users(email);
```

## Migration Notes

### From Numeric IDs to UUIDs
The system supports both numeric IDs and UUIDs:
- **Numeric IDs**: Used internally and for backward compatibility
- **UUIDs**: Used for public-facing APIs and enhanced security
- **Migration**: Existing users automatically get UUIDs assigned
- **No Breaking Changes**: All existing endpoints continue to work

## Testing

### Integration Tests
The system includes comprehensive integration tests covering:
- Complete user registration and verification flows
- UUID-based operations
- Error handling and edge cases
- Email verification scenarios
- Database consistency checks

### Test Data
Test users are created with predictable patterns:
- Usernames: `test_user_{timestamp}`
- Emails: `test.{timestamp}@example.com`
- UUIDs: Automatically generated for each user

## Support

For API support and questions:
- Email: support@example.com
- Documentation: This file
- OpenAPI/Swagger: Available at `/swagger-ui.html` when running the application 