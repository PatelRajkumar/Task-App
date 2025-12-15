package com.pm.taskapp.attachment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FileUploadException extends AttachmentException {

    private FileUploadException(String message) {
        super(message, "FILE_UPLOAD_ERROR", HttpStatus.BAD_REQUEST.value());
    }

    private FileUploadException(String message, Throwable cause) {
        super(message, cause, "FILE_UPLOAD_ERROR", HttpStatus.BAD_REQUEST.value());
    }

    public static FileUploadException fileTooLarge(long maxSizeMB) {
        return new FileUploadException(
                "File size exceeds maximum allowed size of " + maxSizeMB + " MB");
    }

    public static FileUploadException invalidMimeType(String mimeType) {
        return new FileUploadException(
                "File type '" + mimeType + "' is not allowed");
    }

    public static FileUploadException invalidFilename(String filename) {
        return new FileUploadException(
                "Invalid filename: '" + filename + "'");
    }

    public static FileUploadException emptyFile() {
        return new FileUploadException("File cannot be empty");
    }
}