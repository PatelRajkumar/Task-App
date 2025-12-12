package com.pm.taskapp.comments.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested comment is not found.
 * Returns 404 NOT FOUND status.
 * 
 * <p>
 * Common scenarios:
 * <ul>
 * <li>Comment ID does not exist</li>
 * <li>Comment was soft-deleted</li>
 * <li>User doesn't have access to the comment</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CommentNotFoundException extends CommentException {

    private CommentNotFoundException(String message) {
        super(message, "COMMENT_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }

    private CommentNotFoundException(String message, Throwable cause) {
        super(message, cause, "COMMENT_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }

    // ========== Factory Methods ==========

    /**
     * Create exception for comment not found by ID.
     * 
     * @param commentId Comment UUID
     * @return CommentNotFoundException
     */
    public static CommentNotFoundException byId(UUID commentId) {
        return new CommentNotFoundException("Comment not found with ID: " + commentId);
    }

    /**
     * Create exception with custom message.
     * 
     * @param message Error message
     * @return CommentNotFoundException
     */
    public static CommentNotFoundException withMessage(String message) {
        return new CommentNotFoundException(message);
    }

    /**
     * Create exception with message and cause.
     * 
     * @param message Error message
     * @param cause   Root cause
     * @return CommentNotFoundException
     */
    public static CommentNotFoundException withCause(String message, Throwable cause) {
        return new CommentNotFoundException(message, cause);
    }
}