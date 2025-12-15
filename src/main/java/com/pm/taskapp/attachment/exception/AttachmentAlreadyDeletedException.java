package com.pm.taskapp.attachment.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class AttachmentAlreadyDeletedException extends AttachmentException {

    private AttachmentAlreadyDeletedException(String message) {
        super(message, "ATTACHMENT_ALREADY_DELETED", HttpStatus.GONE.value());
    }

    public static AttachmentAlreadyDeletedException byId(UUID attachmentId) {
        return new AttachmentAlreadyDeletedException(
                "Attachment with ID " + attachmentId + " has been deleted");
    }

    public static AttachmentAlreadyDeletedException withMessage(String message) {
        return new AttachmentAlreadyDeletedException(message);
    }
}