package com.pm.taskapp.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;

/**
 * Exception thrown when a token has expired.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenExpiredException extends AuthException {

    private Instant expiredAt;
    private String tokenType;

    public TokenExpiredException(String message) {
        super(message, "TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED.value());
    }

    public TokenExpiredException(String tokenType, Instant expiredAt) {
        super(String.format("%s token has expired at %s", tokenType, expiredAt),
                "TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED.value());
        this.tokenType = tokenType;
        this.expiredAt = expiredAt;
    }

    public TokenExpiredException() {
        super("Token has expired", "TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED.value());
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public String getTokenType() {
        return tokenType;
    }

    public static TokenExpiredException accessTokenExpired() {
        return new TokenExpiredException("Access token has expired. Please refresh your token.");
    }

    public static TokenExpiredException refreshTokenExpired() {
        return new TokenExpiredException("Refresh token has expired. Please login again.");
    }
}