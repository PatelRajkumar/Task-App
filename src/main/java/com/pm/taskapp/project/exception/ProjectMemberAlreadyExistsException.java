package com.pm.taskapp.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Exception thrown when attempting to add a user who is already a project member.
 * Returns 409 CONFLICT status.
 * 
 * <p>Common scenarios:
 * <ul>
 *   <li>User already has a role in the project</li>
 *   <li>Duplicate member addition attempt</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ProjectMemberAlreadyExistsException extends ProjectException {

    public ProjectMemberAlreadyExistsException(String message) {
        super(message, "PROJECT_MEMBER_ALREADY_EXISTS", HttpStatus.CONFLICT.value());
    }

    public ProjectMemberAlreadyExistsException(UUID userId) {
        super("User " + userId + " is already a member of this project",
                "PROJECT_MEMBER_ALREADY_EXISTS", HttpStatus.CONFLICT.value());
    }

    public ProjectMemberAlreadyExistsException(String userEmail, String projectKey) {
        super("User " + userEmail + " is already a member of project " + projectKey,
                "PROJECT_MEMBER_ALREADY_EXISTS", HttpStatus.CONFLICT.value());
    }

    public ProjectMemberAlreadyExistsException(String message, Throwable cause) {
        super(message, "PROJECT_MEMBER_ALREADY_EXISTS", HttpStatus.CONFLICT.value(), cause);
    }

    public static ProjectMemberAlreadyExistsException forUser(UUID userId) {
        return new ProjectMemberAlreadyExistsException(userId);
    }
}