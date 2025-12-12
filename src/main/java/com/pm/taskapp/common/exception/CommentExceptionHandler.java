package com.pm.taskapp.common.exception;

import java.time.Instant;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.pm.taskapp.comments.exception.CommentException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommentExceptionHandler {

    /**
     * Fallback handler for ALL CommentException subclasses.
     * Handles:
     * - CommentNotFoundException
     * - CommentAccessDeniedException
     * - CommentAlreadyDeletedException
     * - EmptyCommentContentException
     * - Any other CommentException subclass
     */
    @ExceptionHandler(CommentException.class)
    public ResponseEntity<ErrorResponse> handleCommentException(CommentException ex, WebRequest request) {
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
