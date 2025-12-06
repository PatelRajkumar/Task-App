package com.pm.taskapp.task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class IssueKeyGenerationException extends IssueException {
    public IssueKeyGenerationException(String message) {
        super(message, "ISSUE_KEY_GENERATION_FAILED",
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    public static IssueKeyGenerationException forProject(UUID projectId) {
        return new IssueKeyGenerationException(
                "Failed to generate issue key for project: " + projectId
        );
    }
}