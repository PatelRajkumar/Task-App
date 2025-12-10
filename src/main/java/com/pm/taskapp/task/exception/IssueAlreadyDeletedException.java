package com.pm.taskapp.task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class IssueAlreadyDeletedException extends IssueException{
    public IssueAlreadyDeletedException(String message){
        super(message,"ISSUE_ALREADY_DELETED", HttpStatus.FORBIDDEN.value());
    }

    public IssueAlreadyDeletedException(String message, Throwable cause){
        super(message, cause,"ISSUE_ALREADY_DELETED", HttpStatus.FORBIDDEN.value());
    }

    // IssueAlreadyDeletedException
    public static IssueAlreadyDeletedException byKey(String key) {
        return new IssueAlreadyDeletedException(
                String.format("Issue %s has already been deleted", key)
        );
    }

    public static IssueAlreadyDeletedException byId(UUID issueId) {
        return new IssueAlreadyDeletedException(
                String.format("Issue %s has already been deleted", issueId)
        );
    }

}
