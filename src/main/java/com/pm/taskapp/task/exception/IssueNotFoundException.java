package com.pm.taskapp.task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class IssueNotFoundException extends IssueException {
    public IssueNotFoundException(UUID id) {
        super("Issue not found with ID: " + id, "ISSUE_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }

    public IssueNotFoundException(String key) {
        super("Issue not found with key: " + key, "ISSUE_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }

    public IssueNotFoundException(String message, Throwable cause) {
        super(message, cause, "ISSUE_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }

    public IssueNotFoundException(String message, String errorCode, int httpStatus) {
        super(message, errorCode, httpStatus);
    }

    // In IssueNotFoundException.java
    public static IssueNotFoundException byId(UUID issueId) {
        return new IssueNotFoundException("Issue not found with ID: " + issueId);
    }

    public static IssueNotFoundException byKey(String key) {
        return new IssueNotFoundException("Issue not found with key: " + key);
    }
}
