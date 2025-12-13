package com.pm.taskapp.attachment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileStorageException extends AttachmentException {

    private FileStorageException(String message) {
        super(message, "FILE_STORAGE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    private FileStorageException(String message, Throwable cause) {
        super(message, cause, "FILE_STORAGE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    public static FileStorageException uploadFailed(String reason) {
        return new FileStorageException("Failed to upload file: " + reason);
    }

    public static FileStorageException downloadFailed(String reason) {
        return new FileStorageException("Failed to download file: " + reason);
    }

    public static FileStorageException deleteFailed(String reason) {
        return new FileStorageException("Failed to delete file: " + reason);
    }

    public static FileStorageException s3Error(String message, Throwable cause) {
        return new FileStorageException("S3 operation failed: " + message, cause);
    }
}