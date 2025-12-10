package com.pm.taskapp.task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDueDateException extends IssueException{
    public InvalidDueDateException(String message){
        super(message, "INVALID_DUE_DATE", HttpStatus.BAD_REQUEST.value());
    }
    // InvalidDueDateException
    public static InvalidDueDateException pastDate(LocalDate dueDate) {
        return new InvalidDueDateException(
                String.format("Due date cannot be in the past: %s", dueDate)
        );
    }

}
