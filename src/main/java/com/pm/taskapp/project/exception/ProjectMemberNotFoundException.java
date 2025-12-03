package com.pm.taskapp.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Exception thrown when a project member is not found.
 * Returns 404 NOT FOUND status.
 * 
 * <p>Common scenarios:
 * <ul>
 *   <li>User is not a member of the project</li>
 *   <li>Member ID does not exist</li>
 *   <li>Member was removed from project</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProjectMemberNotFoundException extends ProjectException {

    public ProjectMemberNotFoundException(String message) {
        super(message, "PROJECT_MEMBER_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }

    public ProjectMemberNotFoundException(UUID projectId, UUID userId) {
        super("User " + userId + " is not a member of project " + projectId,
                "PROJECT_MEMBER_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }

    public ProjectMemberNotFoundException(String message, Throwable cause) {
        super(message, "PROJECT_MEMBER_NOT_FOUND", HttpStatus.NOT_FOUND.value(), cause);
    }

    public static ProjectMemberNotFoundException notMember() {
        return new ProjectMemberNotFoundException("User is not a member of this project");
    }

    public static ProjectMemberNotFoundException byIds(UUID projectId, UUID userId) {
        return new ProjectMemberNotFoundException(projectId, userId);
    }
}