package com.pm.taskapp.common.exception;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
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
import com.pm.taskapp.auth.exception.AccountDisabledException;
import com.pm.taskapp.auth.exception.AccountLockedException;
import com.pm.taskapp.auth.exception.AuthException;
import com.pm.taskapp.auth.exception.DuplicateResourceException;
import com.pm.taskapp.auth.exception.EmailNotVerifiedException;
import com.pm.taskapp.auth.exception.ForbiddenException;
import com.pm.taskapp.auth.exception.InvalidCredentialsException;
import com.pm.taskapp.auth.exception.InvalidPasswordException;
import com.pm.taskapp.auth.exception.InvalidTokenException;
import com.pm.taskapp.auth.exception.TokenExpiredException;
import com.pm.taskapp.project.exception.InvalidRoleAssignmentException;
import com.pm.taskapp.project.exception.LastOwnerRemovalException;
import com.pm.taskapp.project.exception.ProjectAccessDeniedException;
import com.pm.taskapp.project.exception.ProjectException;
import com.pm.taskapp.project.exception.ProjectMemberAlreadyExistsException;
import com.pm.taskapp.project.exception.ProjectMemberNotFoundException;
import com.pm.taskapp.project.exception.ProjectNotFoundException;

/**
 * Global exception handler for the entire application.
 * Catches and handles all exceptions thrown throughout the application,
 * providing consistent error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * Handles AccountDisabledException
         */
        @ExceptionHandler(AccountDisabledException.class)
        public ResponseEntity<ErrorResponse> handleAccountDisabledException(
                        AccountDisabledException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Forbidden")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        /**
         * Handles AccountLockedException
         */
        @ExceptionHandler(AccountLockedException.class)
        public ResponseEntity<ErrorResponse> handleAccountLockedException(
                        AccountLockedException ex, WebRequest request) {

                Map<String, Object> details = new HashMap<>();
                if (ex.getLockedUntil() != null) {
                        details.put("lockedUntil", ex.getLockedUntil());
                }
                if (ex.getReason() != null) {
                        details.put("reason", ex.getReason());
                }

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Forbidden")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .details(details.isEmpty() ? null : details)
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        /**
         * Handles DuplicateResourceException
         */
        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
                        DuplicateResourceException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.CONFLICT.value())
                                .error("Conflict")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        /**
         * Handles EmailNotVerifiedException
         */
        @ExceptionHandler(EmailNotVerifiedException.class)
        public ResponseEntity<ErrorResponse> handleEmailNotVerifiedException(
                        EmailNotVerifiedException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Forbidden")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        /**
         * Handles ForbiddenException
         */
        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ErrorResponse> handleForbiddenException(
                        ForbiddenException ex, WebRequest request) {

                Map<String, Object> details = new HashMap<>();
                if (ex.getRequiredPermission() != null) {
                        details.put("requiredPermission", ex.getRequiredPermission());
                }

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Forbidden")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .details(details.isEmpty() ? null : details)
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        /**
         * Handles InvalidCredentialsException
         */
        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
                        InvalidCredentialsException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("Unauthorized")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handles InvalidPasswordException
         */
        @ExceptionHandler(InvalidPasswordException.class)
        public ResponseEntity<ErrorResponse> handleInvalidPasswordException(
                        InvalidPasswordException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Bad Request")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handles InvalidTokenException
         */
        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<ErrorResponse> handleInvalidTokenException(
                        InvalidTokenException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("Unauthorized")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handles TokenExpiredException
         */
        @ExceptionHandler(TokenExpiredException.class)
        public ResponseEntity<ErrorResponse> handleTokenExpiredException(
                        TokenExpiredException ex, WebRequest request) {

                Map<String, Object> details = new HashMap<>();
                if (ex.getExpiredAt() != null) {
                        details.put("expiredAt", ex.getExpiredAt());
                }
                if (ex.getTokenType() != null) {
                        details.put("tokenType", ex.getTokenType());
                }

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("Unauthorized")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .details(details.isEmpty() ? null : details)
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handles base AuthException
         */
        @ExceptionHandler(AuthException.class)
        public ResponseEntity<ErrorResponse> handleAuthException(
                        AuthException ex, WebRequest request) {

                HttpStatus status = HttpStatus.valueOf(ex.getHttpStatus());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(status.value())
                                .error(status.getReasonPhrase())
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, status);
        }

        /**
         * Handles Spring Security BadCredentialsException
         */
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(
                        BadCredentialsException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("Unauthorized")
                                .message("Invalid email or password")
                                .errorCode("INVALID_CREDENTIALS")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handles Spring Security DisabledException
         */
        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<ErrorResponse> handleDisabledException(
                        DisabledException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Forbidden")
                                .message("Account is disabled. Please contact support for assistance.")
                                .errorCode("ACCOUNT_DISABLED")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        /**
         * Handles Spring Security LockedException
         */
        @ExceptionHandler(LockedException.class)
        public ResponseEntity<ErrorResponse> handleLockedException(
                        LockedException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Forbidden")
                                .message("Account is locked due to multiple failed login attempts")
                                .errorCode("ACCOUNT_LOCKED")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
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
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        /**
         * Handles generic Spring Security AuthenticationException
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthenticationException(
                        AuthenticationException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("Unauthorized")
                                .message("Authentication failed: " + ex.getMessage())
                                .errorCode("AUTHENTICATION_FAILED")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handles validation errors (e.g., @Valid annotation)
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        MethodArgumentNotValidException ex, WebRequest request) {

                Map<String, String> validationErrors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        validationErrors.put(fieldName, errorMessage);
                });

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Bad Request")
                                .message("Validation failed")
                                .errorCode("VALIDATION_ERROR")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .details(Map.of("validationErrors", validationErrors))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handles type mismatch errors
         */
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatchException(
                        MethodArgumentTypeMismatchException ex, WebRequest request) {

                String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                                ex.getValue(), ex.getName(),
                                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Bad Request")
                                .message(message)
                                .errorCode("TYPE_MISMATCH")
                                .path(request.getDescription(false).replace("uri=", ""))
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
                                .message(String.format("No handler found for %s %s", ex.getHttpMethod(),
                                                ex.getRequestURL()))
                                .errorCode("ENDPOINT_NOT_FOUND")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
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
                                .path(request.getDescription(false).replace("uri=", ""))
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
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(ProjectNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleProjectNotFoundException(
                        ProjectNotFoundException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("Not Found")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(ProjectAccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleProjectAccessDeniedException(
                        ProjectAccessDeniedException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Forbidden")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex,
                        WebRequest request) {

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
                                errorCode = "INVALID_FIELD_TYPE";
                        }
                } else if (cause instanceof JsonParseException) {
                        message = "Malformed JSON request";
                        errorCode = "MALFORMED_JSON";
                } else if (cause instanceof MismatchedInputException mismatchedEx) {
                        message = String.format(
                                        "Invalid input for field '%s'",
                                        getFieldName(mismatchedEx));
                        errorCode = "INVALID_INPUT";
                }

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message(message)
                                .errorCode(errorCode)
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        /**
         * Helper method to extract field name from Jackson exceptions
         */
        private String getFieldName(InvalidFormatException ex) {
                if (ex.getPath() != null && !ex.getPath().isEmpty()) {
                        return ex.getPath().get(ex.getPath().size() - 1).getFieldName();
                }
                return "unknown";
        }

        /**
         * Helper method to extract field name from MismatchedInputException
         */
        private String getFieldName(MismatchedInputException ex) {
                if (ex.getPath() != null && !ex.getPath().isEmpty()) {
                        return ex.getPath().get(ex.getPath().size() - 1).getFieldName();
                }
                return "unknown";
        }

        /**
         * Helper method to extract request path from WebRequest
         */
        private String getRequestPath(WebRequest request) {
                return request.getDescription(false).replace("uri=", "");
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
                        DataIntegrityViolationException ex,
                        WebRequest request) {

                String message = "Data integrity constraint violation";
                String errorCode = "DATA_INTEGRITY_VIOLATION";
                HttpStatus status = HttpStatus.BAD_REQUEST;

                // Get the root cause
                Throwable rootCause = getRootCause(ex);
                String rootMessage = rootCause.getMessage();

                if (rootMessage != null) {
                        // Handle PostgreSQL trigger for last owner removal
                        if (rootMessage.contains("prevent_last_owner_removal") ||
                                        rootMessage.contains(
                                                        "Cannot remove or change role of the last project owner")) {

                                message = "Cannot remove the last owner. Transfer ownership to another member before removing this owner.";
                                errorCode = "LAST_OWNER_REMOVAL";
                                status = HttpStatus.BAD_REQUEST;
                        }
                        // Handle unique constraint violations
                        else if (rootMessage.contains("duplicate key") || rootMessage.contains("unique constraint")) {
                                message = parseUniqueConstraintMessage(rootMessage);
                                errorCode = "DUPLICATE_RESOURCE";
                                status = HttpStatus.CONFLICT;
                        }
                        // Handle foreign key violations
                        else if (rootMessage.contains("foreign key constraint")) {
                                message = parseForeignKeyMessage(rootMessage);
                                errorCode = "REFERENCED_RESOURCE_NOT_FOUND";
                                status = HttpStatus.BAD_REQUEST;
                        }
                        // Handle check constraint violations
                        else if (rootMessage.contains("check constraint")) {
                                message = parseCheckConstraintMessage(rootMessage);
                                errorCode = "CONSTRAINT_VIOLATION";
                                status = HttpStatus.BAD_REQUEST;
                        }
                        // Handle other constraint violations
                        else if (rootMessage.contains("could not execute statement")) {
                                // Extract meaningful message from constraint error
                                if (rootMessage.contains("ERROR:")) {
                                        int errorStart = rootMessage.indexOf("ERROR:");
                                        int errorEnd = rootMessage.indexOf("\n", errorStart);
                                        if (errorEnd > errorStart) {
                                                String errorMsg = rootMessage.substring(errorStart + 6, errorEnd)
                                                                .trim();
                                                // Clean up the message
                                                if (errorMsg.length() > 0) {
                                                        message = errorMsg;
                                                        // Remove technical details
                                                        if (message.contains("  Hint:")) {
                                                                message = message.substring(0,
                                                                                message.indexOf("  Hint:")).trim();
                                                        }
                                                }
                                        }
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
         * Handle missing required request parameters
         * Example: /api/projects/search (missing ?searchTerm=...)
         */
        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
                        MissingServletRequestParameterException ex,
                        WebRequest request) {


                String parameterName = ex.getParameterName();
                String parameterType = ex.getParameterType();

                String message = String.format(
                                "Required request parameter '%s' is missing",
                                parameterName);

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message(message)
                                .errorCode("MISSING_REQUEST_PARAMETER")
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ProjectMemberNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleProjectMemberNotFoundException(
                        ProjectMemberNotFoundException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("Not Found")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(ProjectMemberAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleProjectMemberAlreadyExistsException(
                        ProjectMemberAlreadyExistsException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.CONFLICT.value())
                                .error("Conflict")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(LastOwnerRemovalException.class)
        public ResponseEntity<ErrorResponse> handleLastOwnerRemovalException(
                        LastOwnerRemovalException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Bad Request")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(InvalidRoleAssignmentException.class)
        public ResponseEntity<ErrorResponse> handleInvalidRoleAssignmentException(
                        InvalidRoleAssignmentException ex, WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Bad Request")
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ProjectException.class)
        public ResponseEntity<ErrorResponse> handleProjectException(
                        ProjectException ex, WebRequest request) {

                HttpStatus status = HttpStatus.valueOf(ex.getHttpStatus());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(status.value())
                                .error(status.getReasonPhrase())
                                .message(ex.getMessage())
                                .errorCode(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, status);
        }

        /**
         * Handles all other exceptions not specifically handled
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
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                // Log the full exception for debugging
                ex.printStackTrace();

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}