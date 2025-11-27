package com.pm.taskapp.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;

/**
 * Exception thrown when an account is locked due to security reasons.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountLockedException extends AuthException {

    private Instant lockedUntil;
    private String reason;

    public AccountLockedException(String message) {
        super(message, "ACCOUNT_LOCKED", HttpStatus.FORBIDDEN.value());
    }

    public AccountLockedException(String message, Instant lockedUntil) {
        super(message, "ACCOUNT_LOCKED", HttpStatus.FORBIDDEN.value());
        this.lockedUntil = lockedUntil;
    }

    public AccountLockedException(String message, String reason) {
        super(message, "ACCOUNT_LOCKED", HttpStatus.FORBIDDEN.value());
        this.reason = reason;
    }

    public AccountLockedException(String message, Instant lockedUntil, String reason) {
        super(message, "ACCOUNT_LOCKED", HttpStatus.FORBIDDEN.value());
        this.lockedUntil = lockedUntil;
        this.reason = reason;
    }

    public AccountLockedException() {
        super("Account is locked due to multiple failed login attempts",
                "ACCOUNT_LOCKED", HttpStatus.FORBIDDEN.value());
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public String getReason() {
        return reason;
    }
}