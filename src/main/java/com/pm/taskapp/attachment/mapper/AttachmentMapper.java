package com.pm.taskapp.attachment.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.pm.taskapp.attachment.dto.AttachmentResponseDTO;
import com.pm.taskapp.attachment.dto.AttachmentSummaryDTO;
import com.pm.taskapp.attachment.entity.Attachment;
import com.pm.taskapp.project.mapper.ProjectMapper;
import com.pm.taskapp.task.mapper.IssueMapper;

@Component
public class AttachmentMapper {

    private final IssueMapper issueMapper;
    private final ProjectMapper projectMapper;

    public AttachmentMapper(IssueMapper issueMapper, ProjectMapper projectMapper) {
        this.issueMapper = issueMapper;
        this.projectMapper = projectMapper;
    }

    public AttachmentResponseDTO toResponseDTO(Attachment attachment, String downloadUrl) {
        if (attachment == null) {
            return null;
        }

        return AttachmentResponseDTO.builder()
                .id(attachment.getId())
                .issue(issueMapper.toSummaryDTO(attachment.getIssue()))
                .originalFilename(attachment.getOriginalFilename())
                .filename(attachment.getFilename())
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .uploadedBy(projectMapper.toUserSummaryDTO(attachment.getUploadedBy()))
                .storagePath(attachment.getStoragePath())
                .storageType(attachment.getStorageType())
                .createdAt(attachment.getCreatedAt())
                .downloadUrl(downloadUrl)
                .build();
    }

    public AttachmentSummaryDTO toSummaryDTO(Attachment attachment) {
        if (attachment == null) {
            return null;
        }

        return AttachmentSummaryDTO.builder()
                .id(attachment.getId())
                .originalFilename(attachment.getOriginalFilename())
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .uploadedBy(projectMapper.toUserSummaryDTO(attachment.getUploadedBy()))
                .createdAt(attachment.getCreatedAt())
                .build();
    }

    public static String generateS3Key(UUID projectId, UUID issueId, String sanitizedFilename) {
        if (projectId == null || issueId == null || sanitizedFilename == null) {
            throw new IllegalArgumentException("projectId, issueId, and sanitizedFilename cannot be null");
        }

        String uniqueId = UUID.randomUUID().toString();
        return String.format("attachments/%s/%s/%s-%s",
                projectId,
                issueId,
                uniqueId,
                sanitizedFilename);
    }

    public static String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "attachment_" + System.currentTimeMillis();
        }

        // Trim whitespace
        String sanitized = originalFilename.trim();

        // Remove dangerous characters
        sanitized = sanitized.replaceAll("[/\\\\:*?\"<>|]", "");

        // Replace spaces with underscores
        sanitized = sanitized.replaceAll("\\s+", "_");

        // Remove leading/trailing dots and hyphens
        sanitized = sanitized.replaceAll("^[._-]+|[._-]+$", "");

        // Limit to 255 characters (DB column limit)
        if (sanitized.length() > 255) {
            // Keep extension, truncate middle
            int dotIndex = sanitized.lastIndexOf(".");
            if (dotIndex > 0) {
                String extension = sanitized.substring(dotIndex);
                String name = sanitized.substring(0, 255 - extension.length());
                sanitized = name + extension;
            } else {
                sanitized = sanitized.substring(0, 255);
            }
        }

        // If sanitization resulted in empty string, use timestamp
        if (sanitized.isEmpty()) {
            return "attachment_" + System.currentTimeMillis();
        }

        return sanitized;
    }

    public static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        int lastDot = filename.lastIndexOf(".");
        return filename.substring(lastDot + 1).toLowerCase();
    }

}
