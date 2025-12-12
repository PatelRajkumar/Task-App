package com.pm.taskapp.comments.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CommentAccessDeniedException extends CommentException {
    private CommentAccessDeniedException(String message) {
        super(message, "COMMENT_ACCESS_DENIED", HttpStatus.FORBIDDEN.value());
    }

    public static CommentAccessDeniedException notAuthor() {
        return new CommentAccessDeniedException(
            "Only the comment author can edit or delete this comment"
        );
    }

    public static CommentAccessDeniedException insufficientPermissions() {
        return new CommentAccessDeniedException(
            "You don't have permission to perform this action on this comment"
        );
    }

    public static CommentAccessDeniedException notProjectMember() {
        return new CommentAccessDeniedException(
            "You must be a project member to comment on issues"
        );
    }
}
