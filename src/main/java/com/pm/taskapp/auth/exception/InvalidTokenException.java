package com.pm.taskapp.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a token is invalid or expired.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends AuthException {

    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN", HttpStatus.UNAUTHORIZED.value());
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, "INVALID_TOKEN", HttpStatus.UNAUTHORIZED.value(), cause);
    }

    public InvalidTokenException() {
        super("Invalid or expired token", "INVALID_TOKEN", HttpStatus.UNAUTHORIZED.value());
    }

    public static InvalidTokenException expired() {
        return new InvalidTokenException("Token has expired");
    }

    public static InvalidTokenException malformed() {
        return new InvalidTokenException("Token is malformed");
    }

    public static InvalidTokenException unsupported() {
        return new InvalidTokenException("Token type is not supported");
    }
}