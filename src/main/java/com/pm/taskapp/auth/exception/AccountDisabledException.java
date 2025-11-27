package com.pm.taskapp.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to access a disabled account.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountDisabledException extends AuthException {

    public AccountDisabledException(String message) {
        super(message, "ACCOUNT_DISABLED", HttpStatus.FORBIDDEN.value());
    }

    public AccountDisabledException() {
        super("Account is disabled. Please contact support for assistance.",
                "ACCOUNT_DISABLED", HttpStatus.FORBIDDEN.value());
    }

    public AccountDisabledException(String message, Throwable cause) {
        super(message, "ACCOUNT_DISABLED", HttpStatus.FORBIDDEN.value(), cause);
    }
}