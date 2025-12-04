package com.pm.taskapp.common.exception;

import java.time.Instant;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.pm.taskapp.project.exception.ProjectException;

/**
 * Exception handler for project-related exceptions.
 * Handles all exceptions from the project module (com.pm.taskapp.project).
 */
@RestControllerAdvice(basePackages = "com.pm.taskapp.project")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProjectExceptionHandler {

    /**
     * Fallback handler for ALL ProjectException subclasses.
     * Handles:
     * - ProjectNotFoundException
     * - ProjectAccessDeniedException
     * - InvalidRoleAssignmentException
     * - LastOwnerRemovalException
     * - ProjectMemberNotFoundException
     * - ProjectMemberAlreadyExistsException
     * - Any other ProjectException subclass
     */
    @ExceptionHandler(ProjectException.class)
    public ResponseEntity<ErrorResponse> handleProjectException(
            ProjectException ex, WebRequest request) {

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