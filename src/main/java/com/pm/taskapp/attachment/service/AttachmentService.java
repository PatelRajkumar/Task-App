package com.pm.taskapp.attachment.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.pm.taskapp.attachment.dto.AttachmentResponseDTO;
import com.pm.taskapp.attachment.dto.AttachmentSummaryDTO;

public interface AttachmentService {

    AttachmentResponseDTO uploadAttachment(UUID issueId, MultipartFile file, UUID currentUserId);

    AttachmentResponseDTO getAttachment(UUID attachmentId, UUID currentUserId);

    Page<AttachmentSummaryDTO> getIssueAttachments(UUID issueId, Pageable pageable, UUID currentUserId);

    void deleteAttachment(UUID attachmentId, UUID currentUserId);

    String generateDownloadUrl(UUID attachmentId, UUID currentUserId);

}
