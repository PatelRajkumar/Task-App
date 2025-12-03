package com.pm.taskapp.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Exception thrown when a requested project is not found.
 * Returns 404 NOT FOUND status.
 * 
 * <p>Common scenarios:
 * <ul>
 *   <li>Project ID does not exist</li>
 *   <li>Project key does not exist</li>
 *   <li>User doesn't have access to view the project</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProjectNotFoundException extends ProjectException {

    public ProjectNotFoundException(String message) {
        super(message, "PROJECT_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }

    public ProjectNotFoundException(UUID projectId) {
        super("Project not found with ID: " + projectId,
                "PROJECT_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }

    public ProjectNotFoundException(String projectKey, boolean isKey) {
        super("Project not found with key: " + projectKey,
                "PROJECT_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }

    public ProjectNotFoundException(String message, Throwable cause) {
        super(message, "PROJECT_NOT_FOUND", HttpStatus.NOT_FOUND.value(), cause);
    }

    public static ProjectNotFoundException byId(UUID projectId) {
        return new ProjectNotFoundException(projectId);
    }

    public static ProjectNotFoundException byKey(String projectKey) {
        return new ProjectNotFoundException(projectKey, true);
    }
}