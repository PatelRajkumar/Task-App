package com.pm.taskapp.attachment.exception;

public class AttachmentException extends RuntimeException {
    private String errorCode;
    private int httpStatus;

    public AttachmentException(String message) {
        super(message);
        this.httpStatus = 400;
    }

    public AttachmentException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 400;
    }

    public AttachmentException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public AttachmentException(String message, Throwable cause, String errorCode, int httpStatus) {
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
