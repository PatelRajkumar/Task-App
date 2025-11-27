package com.pm.taskapp.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Duration;
import java.time.Instant;

/**
 * Exception thrown when rate limit is exceeded.
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyRequestsException extends AuthException {

    private Instant retryAfter;
    private long remainingTime;
    private String limitType;

    public TooManyRequestsException(String message) {
        super(message, "RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS.value());
    }

    public TooManyRequestsException(String message, Instant retryAfter) {
        super(message, "RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS.value());
        this.retryAfter = retryAfter;
        this.remainingTime = Duration.between(Instant.now(), retryAfter).getSeconds();
    }

    public TooManyRequestsException(String limitType, long remainingSeconds) {
        super(String.format("Too many %s requests. Please wait %d seconds before trying again.",
                        limitType, remainingSeconds),
                "RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS.value());
        this.limitType = limitType;
        this.remainingTime = remainingSeconds;
        this.retryAfter = Instant.now().plusSeconds(remainingSeconds);
    }

    public TooManyRequestsException() {
        super("Too many requests. Please try again later.",
                "RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS.value());
    }

    public Instant getRetryAfter() {
        return retryAfter;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public String getLimitType() {
        return limitType;
    }

    public static TooManyRequestsException loginAttempts(long waitSeconds) {
        return new TooManyRequestsException("login", waitSeconds);
    }

    public static TooManyRequestsException passwordReset(long waitSeconds) {
        return new TooManyRequestsException("password reset", waitSeconds);
    }

    public static TooManyRequestsException emailVerification(long waitSeconds) {
        return new TooManyRequestsException("email verification", waitSeconds);
    }
}