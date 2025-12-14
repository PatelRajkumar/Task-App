package com.pm.taskapp.attachment.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pm.taskapp.attachment.dto.AttachmentResponseDTO;
import com.pm.taskapp.attachment.dto.AttachmentSummaryDTO;
import com.pm.taskapp.attachment.entity.Attachment;
import com.pm.taskapp.attachment.enums.StorageType;
import com.pm.taskapp.attachment.exception.AttachmentAccessDeniedException;
import com.pm.taskapp.attachment.exception.AttachmentAlreadyDeletedException;
import com.pm.taskapp.attachment.exception.AttachmentNotFoundException;
import com.pm.taskapp.attachment.mapper.AttachmentMapper;
import com.pm.taskapp.attachment.repository.AttachmentRepository;
import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.auth.repository.UserRepository;
import com.pm.taskapp.project.repository.ProjectMemberRepository;
import com.pm.taskapp.task.entity.Issue;
import com.pm.taskapp.task.exception.IssueNotFoundException;
import com.pm.taskapp.task.repository.IssueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AttachmentService for attachment management operations.
 * Orchestrates file validation, storage, and database operations.
 *
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Coordinate FileValidationService and FileStorageService</li>
 * <li>Enforce authorization (project membership)</li>
 * <li>Manage database transactions</li>
 * <li>Generate storage keys and download URLs</li>
 * <li>Handle soft deletes</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentServiceImpl implements AttachmentService {

    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentMapper attachmentMapper;
    private final FileValidationService fileValidationService;
    private final FileStorageService fileStorageService;

    @Value("${aws.s3.presigned-url-expiration-hours:1}")
    private int presignedUrlExpirationHours;

    @Value("${app.file-upload.storage-type}")
    private String storageType;

    @Override
    public AttachmentResponseDTO uploadAttachment(
            UUID issueId,
            MultipartFile file,
            UUID currentUserId) {

        log.info("Uploading attachment '{}' to issue: {} by user: {}",
                file.getOriginalFilename(), issueId, currentUserId);

        // 1. Validate current user exists
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with Id: " + currentUserId));

        // 2. Validate issue exists (lazy load, no EntityGraph needed)
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> IssueNotFoundException.byId(issueId));

        // 3. Validate user is a project member (authorization)
        UUID issueProjectId = issue.getProject().getId();
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(issueProjectId, currentUserId)) {
            log.warn("User {} attempted to upload attachment to issue {} without project membership",
                    currentUserId, issueId);
            throw AttachmentAccessDeniedException.notProjectMember();
        }

        // 4. Validate file (size, MIME type, content)
        fileValidationService.validateFile(file);

        // 5. Generate unique storage key
        String originalFilename = file.getOriginalFilename();
        String sanitizedFilename = fileValidationService.sanitizeFilename(originalFilename);
        UUID attachmentId = UUID.randomUUID();

        String storageKey = AttachmentMapper.generateS3Key(
                issueProjectId,
                issueId,
                attachmentId + "-" + sanitizedFilename);

        log.debug("Generated storage key: {}", storageKey);

        // 6. Upload file to storage (S3 or Local)
        String storagePath = fileStorageService.uploadFile(file, storageKey);

        // 7. Determine storage type
        StorageType storageTypeEnum = StorageType.fromString(storageType);

        // 8. Create attachment entity
        Attachment attachment = Attachment.builder()
                .issue(issue)
                .originalFilename(originalFilename)
                .filename(sanitizedFilename)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .uploadedBy(currentUser)
                .storagePath(storagePath)
                .storageType(storageTypeEnum)
                .isDeleted(false)
                .build();

        // 9. Save to database
        Attachment savedAttachment = attachmentRepository.save(attachment);

        log.info("Successfully uploaded attachment: {} (ID: {}, Size: {} bytes)",
                originalFilename, savedAttachment.getId(), file.getSize());

        // 10. Generate download URL
        String downloadUrl = generateDownloadUrlInternal(savedAttachment);

        // 11. Convert to DTO and return
        return attachmentMapper.toResponseDTO(savedAttachment, downloadUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentResponseDTO getAttachment(UUID attachmentId, UUID currentUserId) {
        log.info("Fetching attachment: {} for user: {}", attachmentId, currentUserId);

        // 1. Validate current user exists
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with Id: " + currentUserId));

        // 2. Find attachment with details (EntityGraph for issue and uploadedBy)
        Attachment attachment = attachmentRepository.findByIdWithDetails(attachmentId)
                .orElseThrow(() -> AttachmentNotFoundException.byId(attachmentId));

        // 3. Check if attachment is soft-deleted
        if (attachment.isDeleted()) {
            log.warn("Attempted to access deleted attachment: {}", attachmentId);
            throw AttachmentAlreadyDeletedException.byId(attachmentId);
        }

        // 4. Validate user is a project member (authorization)
        UUID projectId = attachment.getIssue().getProject().getId();
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, currentUserId)) {
            log.warn("User {} attempted to access attachment {} without project membership",
                    currentUserId, attachmentId);
            throw AttachmentAccessDeniedException.notProjectMember();
        }

        // 5. Generate download URL
        String downloadUrl = generateDownloadUrlInternal(attachment);

        // 6. Convert to DTO and return
        log.debug("Successfully fetched attachment: {}", attachmentId);
        return attachmentMapper.toResponseDTO(attachment, downloadUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttachmentSummaryDTO> getIssueAttachments(
            UUID issueId,
            Pageable pageable,
            UUID currentUserId) {

        log.info("Fetching attachments for issue: {} by user: {}", issueId, currentUserId);

        // 1. Validate current user exists
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with Id: " + currentUserId));

        // 2. Validate issue exists
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> IssueNotFoundException.byId(issueId));

        // 3. Validate user is a project member (authorization)
        UUID projectId = issue.getProject().getId();
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, currentUserId)) {
            log.warn("User {} attempted to list attachments for issue {} without project membership",
                    currentUserId, issueId);
            throw AttachmentAccessDeniedException.notProjectMember();
        }

        // 4. Fetch attachments (paginated, ordered by createdAt DESC)
        Page<Attachment> attachments = attachmentRepository.findByIssue(issueId, pageable);

        // 5. Convert to DTOs
        Page<AttachmentSummaryDTO> result = attachments.map(attachmentMapper::toSummaryDTO);

        log.debug("Found {} attachments for issue: {}", result.getTotalElements(), issueId);
        return result;
    }

    @Override
    public void deleteAttachment(UUID attachmentId, UUID currentUserId) {
        log.info("Deleting attachment: {} by user: {}", attachmentId, currentUserId);

        // 1. Validate current user exists
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with Id: " + currentUserId));

        // 2. Find attachment with details
        Attachment attachment = attachmentRepository.findByIdWithDetails(attachmentId)
                .orElseThrow(() -> AttachmentNotFoundException.byId(attachmentId));

        // 3. Check if already deleted
        if (attachment.isDeleted()) {
            log.warn("Attempted to delete already deleted attachment: {}", attachmentId);
            throw AttachmentAlreadyDeletedException.byId(attachmentId);
        }

        // 4. Authorization: Only uploader can delete (or project admin - future
        // enhancement)
        if (!attachment.getUploadedBy().getId().equals(currentUserId)) {
            log.warn("User {} attempted to delete attachment {} uploaded by {}",
                    currentUserId, attachmentId, attachment.getUploadedBy().getId());
            throw AttachmentAccessDeniedException.notAuthor();
        }

        // 5. Validate user is still a project member
        UUID projectId = attachment.getIssue().getProject().getId();
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, currentUserId)) {
            log.warn("User {} attempted to delete attachment without project membership", currentUserId);
            throw AttachmentAccessDeniedException.notProjectMember();
        }

        // 6. Soft delete (set isDeleted = true)
        attachment.setDeleted(true);
        attachmentRepository.save(attachment);

        log.info("Successfully soft-deleted attachment: {} by user: {}", attachmentId, currentUserId);

        // Note: Physical file is NOT deleted from S3/Local storage
        // This preserves audit trail and allows potential recovery
        // Consider implementing a scheduled job to cleanup old deleted files
    }

    @Override
    @Transactional(readOnly = true)
    public String generateDownloadUrl(UUID attachmentId, UUID currentUserId) {
        log.info("Generating download URL for attachment: {} by user: {}", attachmentId, currentUserId);

        // 1. Validate current user exists
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with Id: " + currentUserId));

        // 2. Find attachment with details
        Attachment attachment = attachmentRepository.findByIdWithDetails(attachmentId)
                .orElseThrow(() -> AttachmentNotFoundException.byId(attachmentId));

        // 3. Check if attachment is soft-deleted
        if (attachment.isDeleted()) {
            log.warn("Attempted to generate download URL for deleted attachment: {}", attachmentId);
            throw AttachmentAlreadyDeletedException.byId(attachmentId);
        }

        // 4. Validate user is a project member (authorization)
        UUID projectId = attachment.getIssue().getProject().getId();
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, currentUserId)) {
            log.warn("User {} attempted to download attachment {} without project membership",
                    currentUserId, attachmentId);
            throw AttachmentAccessDeniedException.notProjectMember();
        }

        // 5. Generate download URL
        String downloadUrl = generateDownloadUrlInternal(attachment);

        log.debug("Successfully generated download URL for attachment: {}", attachmentId);
        return downloadUrl;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Internal method to generate download URL.
     * Used by multiple public methods to avoid duplication.
     *
     * @param attachment Attachment entity
     * @return Download URL
     */
    private String generateDownloadUrlInternal(Attachment attachment) {
        Duration expiration = Duration.ofHours(presignedUrlExpirationHours);
        return fileStorageService.generateDownloadUrl(attachment.getStoragePath(), expiration);
    }

    /**
     * Gets the count of attachments for an issue.
     * Useful for displaying attachment counts in issue lists.
     *
     * @param issueId Issue UUID
     * @return Count of non-deleted attachments
     */
    @Transactional(readOnly = true)
    public long getAttachmentCount(UUID issueId) {
        return attachmentRepository.countByIssue(issueId);
    }

    /**
     * Checks if an attachment exists and is not deleted.
     *
     * @param attachmentId Attachment UUID
     * @return true if exists and not deleted
     */
    @Transactional(readOnly = true)
    public boolean attachmentExists(UUID attachmentId) {
        return attachmentRepository.existsById(attachmentId);
    }
}