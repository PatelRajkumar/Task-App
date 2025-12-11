package com.pm.taskapp.comments.exception;

public class CommentException extends RuntimeException {
    private String errorCode;
    private int httpStatus;

    public CommentException(String message) {
        super(message);
        this.httpStatus = 400;
    }

    public CommentException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 400;
    }

    public CommentException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public CommentException(String message, Throwable cause, String errorCode, int httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
