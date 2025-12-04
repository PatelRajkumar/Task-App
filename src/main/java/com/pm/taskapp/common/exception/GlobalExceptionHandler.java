package com.pm.taskapp.common.exception;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

/**
 * Global exception handler for framework-level and unhandled exceptions.
 * Handles Spring framework exceptions and serves as final fallback.
 *
 * Domain-specific exceptions (Auth, Project) are handled by:
 * - AuthExceptionHandler (for auth module)
 * - ProjectExceptionHandler (for project module)
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, Object> details = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        details.put("fieldErrors", fieldErrors);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .path(getRequestPath(request))
                .details(details)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles type mismatch errors (e.g., passing string when UUID expected)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        String message = String.format(
                "Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .errorCode("TYPE_MISMATCH")
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles malformed JSON or invalid request body
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        String message = "Invalid request format";
        String errorCode = "INVALID_REQUEST_FORMAT";

        // Extract more specific error message if available
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatEx) {
            // Handle enum deserialization errors
            if (invalidFormatEx.getTargetType().isEnum()) {
                String enumName = invalidFormatEx.getTargetType().getSimpleName();
                Object invalidValue = invalidFormatEx.getValue();

                // Get valid enum values
                Object[] enumConstants = invalidFormatEx.getTargetType().getEnumConstants();
                String validValues = Arrays.stream(enumConstants)
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                message = String.format(
                        "Invalid value '%s' for field '%s'. Valid values are: [%s]",
                        invalidValue,
                        getFieldName(invalidFormatEx),
                        validValues);
                errorCode = "INVALID_ENUM_VALUE";
            } else {
                // Handle other type mismatches
                message = String.format(
                        "Invalid value for field '%s'. Expected type: %s",
                        getFieldName(invalidFormatEx),
                        invalidFormatEx.getTargetType().getSimpleName());
                errorCode = "INVALID_FIELD_VALUE";
            }
        } else if (cause instanceof MismatchedInputException mismatchedEx) {
            message = "Invalid JSON structure or missing required fields";
            errorCode = "INVALID_JSON_STRUCTURE";
        } else if (cause instanceof JsonParseException) {
            message = "Malformed JSON request";
            errorCode = "MALFORMED_JSON";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .errorCode(errorCode)
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles missing required request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {

        String message = String.format(
                "Required request parameter '%s' is missing",
                ex.getParameterName());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .errorCode("MISSING_REQUEST_PARAMETER")
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles 404 errors when no handler is found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(String.format("No handler found for %s %s",
                        ex.getHttpMethod(), ex.getRequestURL()))
                .errorCode("ENDPOINT_NOT_FOUND")
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles Spring Security AccessDeniedException
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("You don't have permission to access this resource")
                .errorCode("ACCESS_DENIED")
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles database constraint violations
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {

        String message = "Database constraint violation";
        String errorCode = "DATA_INTEGRITY_VIOLATION";
        HttpStatus status = HttpStatus.CONFLICT;

        // Extract detailed error message
        Throwable rootCause = getRootCause(ex);
        if (rootCause != null) {
            String rootMessage = rootCause.getMessage();
            if (rootMessage != null) {
                rootMessage = rootMessage.toLowerCase();

                // Unique constraint violation
                if (rootMessage.contains("unique") || rootMessage.contains("duplicate")) {
                    message = parseUniqueConstraintMessage(rootMessage);
                    errorCode = "DUPLICATE_RESOURCE";
                    status = HttpStatus.CONFLICT;
                }
                // Foreign key constraint violation
                else if (rootMessage.contains("foreign key") || rootMessage.contains("violates")) {
                    message = parseForeignKeyMessage(rootMessage);
                    errorCode = "INVALID_REFERENCE";
                    status = HttpStatus.BAD_REQUEST;
                }
                // Check constraint violation
                else if (rootMessage.contains("check constraint")) {
                    message = parseCheckConstraintMessage(rootMessage);
                    errorCode = "CONSTRAINT_VIOLATION";
                    status = HttpStatus.BAD_REQUEST;
                }
                // Not null constraint violation
                else if (rootMessage.contains("not null") || rootMessage.contains("null value")) {
                    message = "Required field is missing";
                    errorCode = "REQUIRED_FIELD_MISSING";
                    status = HttpStatus.BAD_REQUEST;
                }
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .errorCode(errorCode)
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handles IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .errorCode("ILLEGAL_ARGUMENT")
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .errorCode("ILLEGAL_STATE")
                .path(getRequestPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Final fallback handler for all unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .errorCode("INTERNAL_ERROR")
                .path(getRequestPath(request))
                .build();

        // Log the full exception for debugging
        ex.printStackTrace();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ==================== Helper Methods ====================

    /**
     * Extract request path from WebRequest
     */
    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    /**
     * Get the root cause of an exception
     */
    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Parse unique constraint violation message
     */
    private String parseUniqueConstraintMessage(String message) {
        if (message.contains("project_key")) {
            return "Project key already exists";
        } else if (message.contains("email")) {
            return "Email already exists";
        } else if (message.contains("project_members")) {
            return "User is already a member of this project";
        }
        return "Duplicate resource";
    }

    /**
     * Parse foreign key constraint violation message
     */
    private String parseForeignKeyMessage(String message) {
        if (message.contains("user_id")) {
            return "Referenced user does not exist";
        } else if (message.contains("project_id")) {
            return "Referenced project does not exist";
        }
        return "Referenced resource does not exist";
    }

    /**
     * Parse check constraint violation message
     */
    private String parseCheckConstraintMessage(String message) {
        return "Data validation failed: check constraint violation";
    }

    /**
     * Extract field name from InvalidFormatException
     */
    private String getFieldName(InvalidFormatException ex) {
        if (ex.getPath() != null && !ex.getPath().isEmpty()) {
            return ex.getPath().get(0).getFieldName();
        }
        return "unknown";
    }
}