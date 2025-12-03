package com.pm.taskapp.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to remove or demote the last project owner.
 * Returns 400 BAD REQUEST status.
 * 
 * <p>Business rule: Every project must have at least one OWNER.
 * This is enforced at both application and database level.
 * 
 * <p>Common scenarios:
 * <ul>
 *   <li>Attempting to remove the only owner</li>
 *   <li>Attempting to change role of only owner</li>
 *   <li>Attempting to delete the only owner</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LastOwnerRemovalException extends ProjectException {

    public LastOwnerRemovalException(String message) {
        super(message, "LAST_OWNER_REMOVAL", HttpStatus.BAD_REQUEST.value());
    }

    public LastOwnerRemovalException() {
        super("Cannot remove or change role of the last project owner. " +
              "Transfer ownership to another member first.",
                "LAST_OWNER_REMOVAL", HttpStatus.BAD_REQUEST.value());
    }

    public LastOwnerRemovalException(String message, Throwable cause) {
        super(message, "LAST_OWNER_REMOVAL", HttpStatus.BAD_REQUEST.value(), cause);
    }

    public static LastOwnerRemovalException cannotRemove() {
        return new LastOwnerRemovalException();
    }

    public static LastOwnerRemovalException cannotChangeRole() {
        return new LastOwnerRemovalException(
                "Cannot change the role of the last project owner. " +
                "Add another owner first or transfer ownership."
        );
    }
}