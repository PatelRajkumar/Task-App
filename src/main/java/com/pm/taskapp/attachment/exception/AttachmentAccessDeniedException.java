package com.pm.taskapp.attachment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AttachmentAccessDeniedException extends AttachmentException {
    private AttachmentAccessDeniedException(String message) {
        super(message, "ATTACHMENT_ACCESS_DENIED", HttpStatus.FORBIDDEN.value());
    }

    public static AttachmentAccessDeniedException notAuthor() {
        return new AttachmentAccessDeniedException(
                "Only the attachment uploader can edit or delete this attachment");
    }

    public static AttachmentAccessDeniedException insufficientPermissions() {
        return new AttachmentAccessDeniedException(
                "You don't have permission to perform this action on this attachment");
    }

    public static AttachmentAccessDeniedException notProjectMember() {
        return new AttachmentAccessDeniedException(
                "You must be a project member to access this attachment");
    }
}
