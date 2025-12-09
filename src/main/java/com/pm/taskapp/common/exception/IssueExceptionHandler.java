package com.pm.taskapp.common.exception;

import java.time.Instant;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.pm.taskapp.task.exception.IssueException;

/**
 * Exception handler for issue/task-related exceptions.
 * Handles all exceptions from the task module (com.pm.taskapp.task).
 */
@RestControllerAdvice(basePackages = "com.pm.taskapp.task")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IssueExceptionHandler {

    /**
     * Fallback handler for ALL IssueException subclasses.
     * Handles:
     * - IssueNotFoundException
     * - IssueAccessDeniedException
     * - IssueAlreadyDeletedException
     * - InvalidDueDateException
     * - InvalidStatusTransitionException
     * - IssueKeyGenerationException
     * - Any other IssueException subclass
     */
    @ExceptionHandler(IssueException.class)
    public ResponseEntity<ErrorResponse> handleIssueException(
            IssueException ex, WebRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getHttpStatus());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }
}