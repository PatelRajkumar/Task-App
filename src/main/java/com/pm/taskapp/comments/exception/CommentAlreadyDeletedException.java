package com.pm.taskapp.comments.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class CommentAlreadyDeletedException extends CommentException {
    private CommentAlreadyDeletedException(String message) {
        super(message, "COMMENT_ALREADY_DELETED", HttpStatus.GONE.value());
    }

    public static CommentAlreadyDeletedException byId(UUID commentId) {
        return new CommentAlreadyDeletedException(
            "Comment with ID " + commentId + " has already been deleted"
        );
    }
}
