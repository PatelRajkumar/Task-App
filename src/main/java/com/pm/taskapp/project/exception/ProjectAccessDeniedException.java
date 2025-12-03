package com.pm.taskapp.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Exception thrown when user doesn't have permission to access a project.
 * Returns 403 FORBIDDEN status.
 * 
 * <p>Common scenarios:
 * <ul>
 *   <li>User is not a member of private project</li>
 *   <li>User doesn't have required role for action</li>
 *   <li>Archived project only accessible to OWNERs</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ProjectAccessDeniedException extends ProjectException {

    public ProjectAccessDeniedException(String message) {
        super(message, "PROJECT_ACCESS_DENIED", HttpStatus.FORBIDDEN.value());
    }

    public ProjectAccessDeniedException(UUID projectId, String action) {
        super("You don't have permission to " + action + " this project",
                "PROJECT_ACCESS_DENIED", HttpStatus.FORBIDDEN.value());
    }

    public ProjectAccessDeniedException(String message, Throwable cause) {
        super(message, "PROJECT_ACCESS_DENIED", HttpStatus.FORBIDDEN.value(), cause);
    }

    public static ProjectAccessDeniedException forAction(String action) {
        return new ProjectAccessDeniedException("You don't have permission to " + action);
    }

    public static ProjectAccessDeniedException notMember() {
        return new ProjectAccessDeniedException("You are not a member of this project");
    }

    public static ProjectAccessDeniedException insufficientRole() {
        return new ProjectAccessDeniedException("Your role doesn't have sufficient permissions for this action");
    }

    public static ProjectAccessDeniedException archivedProject() {
        return new ProjectAccessDeniedException("Only project owners can access archived projects");
    }
}