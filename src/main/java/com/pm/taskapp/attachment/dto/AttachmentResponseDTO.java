package com.pm.taskapp.attachment.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pm.taskapp.attachment.enums.StorageType;
import com.pm.taskapp.project.dto.response.UserSummaryDTO;
import com.pm.taskapp.task.dto.IssueSummaryDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttachmentResponseDTO {
    private UUID id;
    private IssueSummaryDTO issue;
    private String originalFilename;
    private String filename;
    private Long fileSize;
    private String mimeType;
    private UserSummaryDTO uploadedBy;
    private String storagePath;
    private StorageType storageType;
    private Instant createdAt;
    private String downloadUrl;
}
