package com.pm.taskapp.attachment.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pm.taskapp.attachment.dto.AttachmentResponseDTO;
import com.pm.taskapp.attachment.dto.AttachmentSummaryDTO;
import com.pm.taskapp.attachment.service.AttachmentService;
import com.pm.taskapp.auth.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for attachment management operations.
 * Provides endpoints for uploading, retrieving, listing, and deleting
 * attachments.
 *
 * <p>
 * Base Path: {@code /api/issues/{issueId}/attachments}
 *
 * <p>
 * Security: All endpoints require JWT authentication
 *
 * <p>
 * Endpoints:
 * <ul>
 * <li>POST /api/issues/{issueId}/attachments - Upload attachment</li>
 * <li>GET /api/issues/{issueId}/attachments - List attachments (paginated)</li>
 * <li>GET /api/attachments/{attachmentId} - Get attachment details</li>
 * <li>GET /api/attachments/{attachmentId}/download - Generate download URL</li>
 * <li>DELETE /api/attachments/{attachmentId} - Delete attachment (soft
 * delete)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Attachments", description = "Attachment management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * Upload an attachment to an issue.
     *
     * <p>
     * Endpoint: {@code POST /api/issues/{issueId}/attachments}
     *
     * <p>
     * Validations:
     * <ul>
     * <li>File size ≤ 50MB</li>
     * <li>MIME type in whitelist</li>
     * <li>User is project member</li>
     * <li>Issue exists and not deleted</li>
     * </ul>
     *
     * @param issueId       Issue UUID
     * @param file          File to upload (multipart/form-data)
     * @param userPrincipal Authenticated user (injected)
     * @return AttachmentResponseDTO with download URL
     */
    @PostMapping(value = "/issues/{issueId}/attachments")
    @Operation(summary = "Upload attachment to issue", description = "Uploads a file as an attachment to the specified issue. "
            +
            "File is validated for size, type, and content before upload to S3/Local storage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attachment uploaded successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AttachmentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file (size, type, empty)"),
            @ApiResponse(responseCode = "403", description = "User not a project member"),
            @ApiResponse(responseCode = "404", description = "Issue not found"),
            @ApiResponse(responseCode = "500", description = "File storage error")
    })
    public ResponseEntity<AttachmentResponseDTO> uploadAttachment(
            @Parameter(description = "Issue UUID", required = true) @PathVariable UUID issueId,

            @Parameter(description = "File to upload (max 50MB, allowed types: images, PDFs, documents)", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestParam("file") MultipartFile file,

            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Upload attachment request for issue: {} by user: {}", issueId, userPrincipal.getId());

        AttachmentResponseDTO response = attachmentService.uploadAttachment(
                issueId,
                file,
                userPrincipal.getId());

        log.info("Attachment uploaded successfully: {} (ID: {})",
                response.getOriginalFilename(), response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get attachment details with download URL.
     *
     * <p>
     * Endpoint: {@code GET /api/attachments/{attachmentId}}
     *
     * @param attachmentId  Attachment UUID
     * @param userPrincipal Authenticated user (injected)
     * @return AttachmentResponseDTO with download URL
     */
    @GetMapping("/attachments/{attachmentId}")
    @Operation(summary = "Get attachment details", description = "Retrieves attachment metadata including download URL (presigned for S3, valid 1 hour)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AttachmentResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "User not a project member"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "410", description = "Attachment deleted")
    })
    public ResponseEntity<AttachmentResponseDTO> getAttachment(
            @Parameter(description = "Attachment UUID", required = true) @PathVariable UUID attachmentId,

            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Get attachment request: {} by user: {}", attachmentId, userPrincipal.getId());

        AttachmentResponseDTO response = attachmentService.getAttachment(
                attachmentId,
                userPrincipal.getId());

        log.debug("Attachment retrieved: {}", attachmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * List all attachments for an issue (paginated).
     *
     * <p>
     * Endpoint: {@code GET /api/issues/{issueId}/attachments}
     *
     * <p>
     * Default pagination: page=0, size=10, sort=createdAt,DESC
     *
     * @param issueId       Issue UUID
     * @param pageable      Pagination parameters (injected)
     * @param userPrincipal Authenticated user (injected)
     * @return Page of AttachmentSummaryDTO
     */
    @GetMapping("/issues/{issueId}/attachments")
    @Operation(summary = "List issue attachments", description = "Retrieves paginated list of attachments for an issue, ordered by upload date (newest first)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "User not a project member"),
            @ApiResponse(responseCode = "404", description = "Issue not found")
    })
    public ResponseEntity<Page<AttachmentSummaryDTO>> getIssueAttachments(
            @Parameter(description = "Issue UUID", required = true) @PathVariable UUID issueId,

            @Parameter(description = "Pagination parameters (page, size, sort)") @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,

            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("List attachments for issue: {} by user: {} (page: {}, size: {})",
                issueId, userPrincipal.getId(), pageable.getPageNumber(), pageable.getPageSize());

        Page<AttachmentSummaryDTO> response = attachmentService.getIssueAttachments(
                issueId,
                pageable,
                userPrincipal.getId());

        log.debug("Found {} attachments for issue: {}", response.getTotalElements(), issueId);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate download URL for an attachment.
     *
     * <p>
     * Endpoint: {@code GET /api/attachments/{attachmentId}/download}
     *
     * <p>
     * Response: Plain text URL (presigned for S3, valid 1 hour)
     *
     * @param attachmentId  Attachment UUID
     * @param userPrincipal Authenticated user (injected)
     * @return Download URL as plain text
     */
    @GetMapping(value = "/attachments/{attachmentId}/download")
    @Operation(summary = "Generate download URL", description = "Generates a temporary download URL for the attachment. "
            +
            "For S3 storage: presigned URL valid for 1 hour. " +
            "For local storage: application URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Download URL generated", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string", example = "https://s3.amazonaws.com/..."))),
            @ApiResponse(responseCode = "403", description = "User not a project member"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "410", description = "Attachment deleted")
    })
    public ResponseEntity<String> generateDownloadUrl(
            @Parameter(description = "Attachment UUID", required = true) @PathVariable UUID attachmentId,

            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Generate download URL for attachment: {} by user: {}", attachmentId, userPrincipal.getId());

        String downloadUrl = attachmentService.generateDownloadUrl(
                attachmentId,
                userPrincipal.getId());

        log.debug("Download URL generated for attachment: {}", attachmentId);
        return ResponseEntity.ok(downloadUrl);
    }

    /**
     * Delete an attachment (soft delete).
     *
     * <p>
     * Endpoint: {@code DELETE /api/attachments/{attachmentId}}
     *
     * <p>
     * Authorization: Only the uploader can delete
     *
     * <p>
     * Note: Physical file is NOT deleted from storage (audit trail)
     *
     * @param attachmentId  Attachment UUID
     * @param userPrincipal Authenticated user (injected)
     * @return 204 No Content
     */
    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(summary = "Delete attachment", description = "Soft deletes an attachment (sets isDeleted=true). " +
            "Only the uploader can delete. Physical file remains in storage for audit trail.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not the uploader or not project member"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "410", description = "Attachment already deleted")
    })
    public ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "Attachment UUID", required = true) @PathVariable UUID attachmentId,

            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Delete attachment request: {} by user: {}", attachmentId, userPrincipal.getId());

        attachmentService.deleteAttachment(attachmentId, userPrincipal.getId());

        log.info("Attachment deleted successfully: {}", attachmentId);
        return ResponseEntity.noContent().build();
    }
}