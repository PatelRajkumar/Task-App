package com.pm.taskapp.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when email verification is required but not completed.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class EmailNotVerifiedException extends AuthException {

    public EmailNotVerifiedException(String message) {
        super(message, "EMAIL_NOT_VERIFIED", HttpStatus.FORBIDDEN.value());
    }

    public EmailNotVerifiedException() {
        super("Email verification is required. Please check your email for verification link.",
                "EMAIL_NOT_VERIFIED", HttpStatus.FORBIDDEN.value());
    }

    public EmailNotVerifiedException(String email, boolean resendAvailable) {
        super(String.format("Email %s is not verified.%s",
                        email,
                        resendAvailable ? " A new verification email has been sent." : ""),
                "EMAIL_NOT_VERIFIED", HttpStatus.FORBIDDEN.value());
    }
}