package com.pm.taskapp.comments.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmptyCommentContentException extends CommentException {
    private EmptyCommentContentException(String message) {
        super(message, "EMPTY_COMMENT_CONTENT", HttpStatus.BAD_REQUEST.value());
    }

    public static EmptyCommentContentException empty() {
        return new EmptyCommentContentException(
            "Comment content cannot be empty"
        );
    }
}
