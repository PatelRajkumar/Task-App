package com.pm.taskapp.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends AuthException {

    private Map<String, String> errors;

    public ValidationException(String message) {
        super(message, "VALIDATION_FAILED", HttpStatus.BAD_REQUEST.value());
        this.errors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message, "VALIDATION_FAILED", HttpStatus.BAD_REQUEST.value());
        this.errors = errors;
    }

    public ValidationException(BindingResult bindingResult) {
        super("Validation failed", "VALIDATION_FAILED", HttpStatus.BAD_REQUEST.value());
        this.errors = new HashMap<>();

        for (FieldError error : bindingResult.getFieldErrors()) {
            this.errors.put(error.getField(), error.getDefaultMessage());
        }
    }

    public ValidationException(String field, String error) {
        super("Validation failed for field: " + field,
                "VALIDATION_FAILED", HttpStatus.BAD_REQUEST.value());
        this.errors = new HashMap<>();
        this.errors.put(field, error);
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void addError(String field, String message) {
        if (this.errors == null) {
            this.errors = new HashMap<>();
        }
        this.errors.put(field, message);
    }
}