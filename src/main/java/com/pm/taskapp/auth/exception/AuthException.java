package com.pm.taskapp.auth.exception;

/**
 * Base exception class for authentication-related errors.
 */
public class AuthException extends RuntimeException {

    private String errorCode;
    private int httpStatus;

    public AuthException(String message) {
        super(message);
        this.httpStatus = 400;
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 400;
    }

    public AuthException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public AuthException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}