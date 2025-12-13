package com.pm.taskapp.attachment.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pm.taskapp.project.dto.response.UserSummaryDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttachmentSummaryDTO {
    private UUID id;
    private String originalFilename;
    private Long fileSize;
    private String mimeType;
    private UserSummaryDTO uploadedBy;
    private Instant createdAt;
}