package com.pm.taskapp.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response structure for all API errors.
 * Provides consistent error information across the application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp when the error occurred
     */
    private Instant timestamp;

    /**
     * HTTP status code
     */
    private int status;

    /**
     * HTTP status reason phrase (e.g., "Bad Request", "Unauthorized")
     */
    private String error;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Application-specific error code for client handling
     */
    private String errorCode;

    /**
     * Request path that caused the error
     */
    private String path;

    /**
     * Additional error details (optional)
     * Can contain validation errors, extra context, etc.
     */
    private Map<String, Object> details;
}