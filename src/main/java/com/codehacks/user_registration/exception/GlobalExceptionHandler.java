package com.codehacks.user_registration.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * GlobalExceptionHandler - Centralized exception handling for the application
 *
 * This class demonstrates:
 * 1. Global exception handling for all controllers
 * 2. Consistent error response format
 * 3. Proper HTTP status codes for different exception types
 * 4. Detailed logging for debugging
 * 5. User-friendly error messages
 * 6. Validation error handling
 * 7. Email service exception handling
 * 8. Custom application exception handling
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle custom UserRegistrationException
     */
    @ExceptionHandler(UserRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleUserRegistrationException(
            UserRegistrationException ex, WebRequest request) {
        
        log.warn("User registration error: {}", ex.getMessage());
        
        // Determine appropriate status code based on exception message
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getMessage().contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex.getMessage().contains("already") || ex.getMessage().contains("already registered")) {
            status = HttpStatus.CONFLICT;
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error("User Registration Error")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Invalid input data")
                .details(errors)
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        log.warn("Constraint violation: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .message("Data validation failed")
                .details(errors)
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle entity not found exceptions
     */
    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            Exception ex, WebRequest request) {
        
        log.warn("Entity not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle data integrity violations (duplicate keys, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        log.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "Data integrity constraint violated";
        if (ex.getMessage().contains("duplicate key")) {
            message = "Resource already exists with the provided data";
        } else if (ex.getMessage().contains("foreign key")) {
            message = "Referenced resource does not exist";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Error")
                .message(message)
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle email service exceptions
     */
    @ExceptionHandler({MailException.class, MailAuthenticationException.class, 
                      MailSendException.class, MessagingException.class})
    public ResponseEntity<ErrorResponse> handleEmailExceptions(
            Exception ex, WebRequest request) {
        
        log.error("Email service error: {}", ex.getMessage());
        
        String message = "Email service error occurred";
        if (ex instanceof MailAuthenticationException) {
            message = "Email service authentication failed";
        } else if (ex instanceof MailSendException) {
            message = "Failed to send email";
        } else if (ex instanceof MessagingException) {
            message = "Email messaging error";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Email Service Error")
                .message(message)
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Handle method argument type mismatches
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        log.warn("Method argument type mismatch: {}", ex.getMessage());
        
        String message = String.format("Parameter '%s' should be of type %s", 
                ex.getName(), ex.getRequiredType().getSimpleName());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Type Mismatch")
                .message(message)
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Argument")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle illegal state exceptions
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex, WebRequest request) {
        
        log.warn("Illegal state: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid State")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle missing request parameter exceptions
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {
        
        log.warn("Missing request parameter: {}", ex.getMessage());
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Missing Parameter")
                .message(message)
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle HTTP message not readable exceptions (invalid JSON, etc.)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        log.warn("HTTP message not readable: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Request")
                .message("Invalid request format")
                .path(request.getDescription(false))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }
} 