package com.pm.taskapp.attachment.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested attachment is not found.
 * Returns 404 NOT FOUND status.
 * 
 * <p>
 * Common scenarios:
 * <ul>
 * <li>Attachment ID does not exist</li>
 * <li>Attachment was soft-deleted</li>
 * <li>User doesn't have access to the attachment</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AttachmentNotFoundException extends AttachmentException {

    private AttachmentNotFoundException(String message) {
        super(message, "ATTACHMENT_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }

    private AttachmentNotFoundException(String message, Throwable cause) {
        super(message, cause, "ATTACHMENT_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }

    // ========== Factory Methods ==========

    /**
     * Create exception for attachment not found by ID.
     * 
     * @param attachmentId Attachment UUID
     * @return AttachmentNotFoundException
     */
    public static AttachmentNotFoundException byId(UUID attachmentId) {
        return new AttachmentNotFoundException("Attachment not found with ID: " + attachmentId);
    }

    /**
     * Create exception with custom message.
     * 
     * @param message Error message
     * @return AttachmentNotFoundException
     */
    public static AttachmentNotFoundException withMessage(String message) {
        return new AttachmentNotFoundException(message);
    }

    /**
     * Create exception with message and cause.
     * 
     * @param message Error message
     * @param cause   Root cause
     * @return AttachmentNotFoundException
     */
    public static AttachmentNotFoundException withCause(String message, Throwable cause) {
        return new AttachmentNotFoundException(message, cause);
    }
}