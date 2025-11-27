package com.pm.taskapp.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when user lacks required permissions.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends AuthException {

    private String requiredPermission;

    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN.value());
    }

    public ForbiddenException() {
        super("You don't have permission to access this resource",
                "FORBIDDEN", HttpStatus.FORBIDDEN.value());
    }

    public ForbiddenException(String message, String requiredPermission) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN.value());
        this.requiredPermission = requiredPermission;
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN.value(), cause);
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public static ForbiddenException missingPermission(String permission) {
        return new ForbiddenException(
                String.format("Missing required permission: %s", permission),
                permission
        );
    }

    public static ForbiddenException missingRole(String role) {
        return new ForbiddenException(
                String.format("Missing required role: %s", role)
        );
    }
}