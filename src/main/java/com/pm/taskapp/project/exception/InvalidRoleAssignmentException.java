package com.pm.taskapp.project.exception;

import com.pm.taskapp.project.enums.ProjectRole;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting an invalid role assignment.
 * Returns 400 BAD REQUEST status.
 * 
 * <p>Common scenarios:
 * <ul>
 *   <li>Attempting to directly assign OWNER role</li>
 *   <li>Non-ADMIN trying to assign ADMIN role</li>
 *   <li>User doesn't have permission to manage target role</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRoleAssignmentException extends ProjectException {

    public InvalidRoleAssignmentException(String message) {
        super(message, "INVALID_ROLE_ASSIGNMENT", HttpStatus.BAD_REQUEST.value());
    }

    public InvalidRoleAssignmentException(ProjectRole role) {
        super("Role " + role + " cannot be directly assigned",
                "INVALID_ROLE_ASSIGNMENT", HttpStatus.BAD_REQUEST.value());
    }

    public InvalidRoleAssignmentException(String message, Throwable cause) {
        super(message, "INVALID_ROLE_ASSIGNMENT", HttpStatus.BAD_REQUEST.value(), cause);
    }

    public static InvalidRoleAssignmentException ownerRoleNotAssignable() {
        return new InvalidRoleAssignmentException(
                "OWNER role cannot be directly assigned. Use ownership transfer instead."
        );
    }

    public static InvalidRoleAssignmentException insufficientPermissions() {
        return new InvalidRoleAssignmentException(
                "You don't have permission to assign this role"
        );
    }

    public static InvalidRoleAssignmentException cannotManageRole(ProjectRole targetRole) {
        return new InvalidRoleAssignmentException(
                "You don't have permission to manage " + targetRole.getDisplayName() + " role"
        );
    }
}