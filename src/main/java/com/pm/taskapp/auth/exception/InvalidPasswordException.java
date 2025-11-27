package com.pm.taskapp.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when password validation fails.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPasswordException extends AuthException {

    public InvalidPasswordException(String message) {
        super(message, "INVALID_PASSWORD", HttpStatus.BAD_REQUEST.value());
    }

    public InvalidPasswordException(String message, Throwable cause) {
        super(message, "INVALID_PASSWORD", HttpStatus.BAD_REQUEST.value(), cause);
    }

    public InvalidPasswordException() {
        super("Invalid password provided", "INVALID_PASSWORD", HttpStatus.BAD_REQUEST.value());
    }
}