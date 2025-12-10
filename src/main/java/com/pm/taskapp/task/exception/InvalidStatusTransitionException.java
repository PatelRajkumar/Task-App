package com.pm.taskapp.task.exception;

import com.pm.taskapp.task.enums.IssueStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStatusTransitionException extends IssueException{

    public InvalidStatusTransitionException(String message){
        super(message, "INVALID_STATUS_TRANSITION", HttpStatus.BAD_REQUEST.value());
    }

    public static InvalidStatusTransitionException of(IssueStatus from, IssueStatus to) {
        return new InvalidStatusTransitionException(
                String.format("Invalid status transition from %s to %s", from, to)
        );
    }
}
