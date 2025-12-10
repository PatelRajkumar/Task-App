package com.pm.taskapp.task.exception;

public class IssueException extends RuntimeException{
    private String errorCode;
    private int httpStatus;

    public IssueException(String message){
        super(message);
        this.httpStatus = 400;
    }

    public IssueException(String message, Throwable cause){
        super(message, cause);
        this.httpStatus = 400;
    }

    public IssueException(String message, String errorCode, int httpStatus){
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public IssueException(String message, Throwable cause, String errorCode, int httpStatus){
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
