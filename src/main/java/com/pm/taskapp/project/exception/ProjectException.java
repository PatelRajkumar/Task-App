package com.pm.taskapp.project.exception;

/**
 * Base exception class for project-related errors.
 * All project module exceptions extend from this class.
 * 
 * <p>Provides common error handling capabilities:
 * <ul>
 *   <li>Error codes for specific error types</li>
 *   <li>HTTP status codes for responses</li>
 *   <li>Consistent exception structure</li>
 * </ul>
 * 
 * @since 1.0.0
 */
public class ProjectException extends RuntimeException {

    private String errorCode;
    private int httpStatus;

    public ProjectException(String message) {
        super(message);
        this.httpStatus = 400;
    }

    public ProjectException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 400;
    }

    public ProjectException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public ProjectException(String message, String errorCode, int httpStatus, Throwable cause) {
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