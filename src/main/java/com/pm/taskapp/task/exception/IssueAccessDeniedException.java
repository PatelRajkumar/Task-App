package com.pm.taskapp.task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class IssueAccessDeniedException extends IssueException{
    public IssueAccessDeniedException(String message){
        super(message,"ISSUE_ACCESS_DENIED", HttpStatus.FORBIDDEN.value());
    }

    public IssueAccessDeniedException(UUID issueId, String action){
        super("You don't have permission to " + action + " this issue",
                "ISSUE_ACCESS_DENIED", HttpStatus.FORBIDDEN.value());
    }

    public IssueAccessDeniedException(String message, Throwable cause){
        super(message, cause, "ISSUE_ACCESS_DENIED", HttpStatus.FORBIDDEN.value());
    }
}
