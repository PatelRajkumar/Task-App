package com.pm.taskapp.comments.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pm.taskapp.comments.dto.CommentCreateRequestDTO;
import com.pm.taskapp.comments.dto.CommentResponseDTO;
import com.pm.taskapp.comments.dto.CommentSummaryDTO;
import com.pm.taskapp.comments.dto.CommentUpdateRequestDTO;

/**
 * Service interface for comment management.
 * Handles CRUD operations for comments on issues with proper authorization.
 * 
 * <p>
 * Authorization Rules:
 * <ul>
 * <li>Any project member can create comments on issues</li>
 * <li>Only comment author or project OWNER/ADMIN can update comments</li>
 * <li>Only comment author or project OWNER/ADMIN can delete comments (soft
 * delete)</li>
 * <li>Any project member can view comments on issues</li>
 * </ul>
 * 
 * @since 1.0.0
 */
public interface CommentService {

    // ========== CRUD Operations ==========

    /**
     * Create a new comment on an issue.
     * User must be a member of the issue's project.
     * 
     * @param issueId       Issue ID to comment on
     * @param request       Comment creation data
     * @param currentUserId ID of user creating comment
     * @return Created comment details
     * @throws com.pm.taskapp.task.exception.IssueNotFoundException
     * @throws com.pm.taskapp.project.exception.ProjectMemberNotFoundException
     * @throws com.pm.taskapp.comments.exception.EmptyCommentContentException
     */
    CommentResponseDTO createComment(UUID issueId, CommentCreateRequestDTO request, UUID currentUserId);

    /**
     * Update an existing comment.
     * Only comment author or project OWNER/ADMIN can update.
     * 
     * @param commentId     Comment ID to update
     * @param request       Updated comment data
     * @param currentUserId ID of user updating comment
     * @return Updated comment details
     * @throws com.pm.taskapp.comments.exception.CommentAccessDeniedException
     * @throws com.pm.taskapp.comments.exception.CommentAlreadyDeletedException
     */
    CommentResponseDTO updateComment(UUID commentId, CommentUpdateRequestDTO request, UUID currentUserId);

    /**
     * Get comment by ID.
     * User must be a member of the issue's project.
     * 
     * @param commentId     Comment ID
     * @param currentUserId ID of requesting user
     * @return Comment details
     * @throws com.pm.taskapp.comments.exception.CommentNotFoundException
     * @throws com.pm.taskapp.project.exception.ProjectMemberNotFoundException
     */
    CommentResponseDTO getCommentById(UUID commentId, UUID currentUserId);

    /**
     * Soft delete a comment.
     * Only comment author or project OWNER/ADMIN can delete.
     * 
     * @param commentId     Comment ID to delete
     * @param currentUserId ID of user deleting comment
     * @throws com.pm.taskapp.comments.exception.CommentNotFoundException
     * @throws com.pm.taskapp.comments.exception.CommentAccessDeniedException
     * @throws com.pm.taskapp.comments.exception.CommentAlreadyDeletedException
     */
    void deleteComment(UUID commentId, UUID currentUserId);

    // ========== List & Count Operations ==========

    /**
     * Get all comments for an issue (paginated).
     * User must be a member of the issue's project.
     * Comments are ordered by creation time (newest first).
     * 
     * @param issueId       Issue ID
     * @param currentUserId ID of requesting user
     * @param pageable      Pagination parameters
     * @return Page of comment summaries
     * @throws com.pm.taskapp.task.exception.IssueNotFoundException
     * @throws com.pm.taskapp.project.exception.ProjectMemberNotFoundException
     */
    Page<CommentSummaryDTO> getIssueComments(UUID issueId, UUID currentUserId, Pageable pageable);

    /**
     * Count total comments on an issue (excludes soft-deleted).
     * User must be a member of the issue's project.
     * 
     * @param issueId       Issue ID
     * @param currentUserId ID of requesting user
     * @return Comment count
     * @throws com.pm.taskapp.task.exception.IssueNotFoundException
     * @throws com.pm.taskapp.project.exception.ProjectMemberNotFoundException
     */
    long countIssueComments(UUID issueId, UUID currentUserId);

    /**
     * Get all comments by a specific author (paginated).
     * Useful for user activity/history views.
     * 
     * @param authorId      Author user ID
     * @param currentUserId ID of requesting user
     * @param pageable      Pagination parameters
     * @return Page of comment summaries
     */
    Page<CommentSummaryDTO> getUserComments(UUID authorId, UUID currentUserId, Pageable pageable);

}