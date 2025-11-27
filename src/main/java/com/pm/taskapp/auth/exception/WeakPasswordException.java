package com.pm.taskapp.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * Exception thrown when a password doesn't meet security requirements.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WeakPasswordException extends AuthException {

    private List<String> violations;

    public WeakPasswordException(String message) {
        super(message, "WEAK_PASSWORD", HttpStatus.BAD_REQUEST.value());
    }

    public WeakPasswordException(String message, List<String> violations) {
        super(message, "WEAK_PASSWORD", HttpStatus.BAD_REQUEST.value());
        this.violations = violations;
    }

    public WeakPasswordException() {
        super("Password does not meet security requirements",
                "WEAK_PASSWORD", HttpStatus.BAD_REQUEST.value());
    }

    public List<String> getViolations() {
        return violations;
    }

    public static WeakPasswordException withViolations(List<String> violations) {
        String message = "Password does not meet the following requirements: " + String.join(", ", violations);
        return new WeakPasswordException(message, violations);
    }
}