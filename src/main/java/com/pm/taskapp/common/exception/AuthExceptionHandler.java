package com.pm.taskapp.common.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.pm.taskapp.auth.exception.AccountDisabledException;
import com.pm.taskapp.auth.exception.AccountLockedException;
import com.pm.taskapp.auth.exception.AuthException;
import com.pm.taskapp.auth.exception.DuplicateResourceException;
import com.pm.taskapp.auth.exception.EmailNotVerifiedException;
import com.pm.taskapp.auth.exception.ForbiddenException;
import com.pm.taskapp.auth.exception.InvalidCredentialsException;
import com.pm.taskapp.auth.exception.InvalidPasswordException;
import com.pm.taskapp.auth.exception.InvalidTokenException;
import com.pm.taskapp.auth.exception.ResourceNotFoundException;
import com.pm.taskapp.auth.exception.TokenExpiredException;
import com.pm.taskapp.auth.exception.ValidationException;
import com.pm.taskapp.common.exception.ErrorResponse;

/**
 * Exception handler for authentication and authorization related exceptions.
 * Handles all exceptions from the auth module (com.pm.taskapp.auth).
 */
@RestControllerAdvice(basePackages = "com.pm.taskapp.auth")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {

    /**
     * Handles AccountLockedException - Special case with additional details
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
     * Handles TokenExpiredException - Special case with additional details
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
     * Handles ForbiddenException - Special case with required permission
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
     * Fallback handler for ALL AuthException subclasses.
     * Handles:
     * - AccountDisabledException (if not caught by specific handler above)
     * - InvalidCredentialsException
     * - InvalidPasswordException
     * - InvalidTokenException
     * - DuplicateResourceException
     * - EmailNotVerifiedException
     * - ResourceNotFoundException
     * - ValidationException
     * - Any other AuthException subclass
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
                .message("Account is locked. Please try again later or contact support.")
                .errorCode("ACCOUNT_LOCKED")
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
                .message("Authentication failed")
                .errorCode("AUTHENTICATION_FAILED")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}