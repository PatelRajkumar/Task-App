package com.pm.taskapp.comments.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.auth.repository.UserRepository;
import com.pm.taskapp.comments.dto.CommentCreateRequestDTO;
import com.pm.taskapp.comments.dto.CommentResponseDTO;
import com.pm.taskapp.comments.dto.CommentSummaryDTO;
import com.pm.taskapp.comments.dto.CommentUpdateRequestDTO;
import com.pm.taskapp.comments.entity.Comment;
import com.pm.taskapp.comments.exception.CommentAccessDeniedException;
import com.pm.taskapp.comments.exception.CommentAlreadyDeletedException;
import com.pm.taskapp.comments.exception.CommentNotFoundException;
import com.pm.taskapp.comments.exception.EmptyCommentContentException;
import com.pm.taskapp.comments.mapper.CommentMapper;
import com.pm.taskapp.comments.repository.CommentRepository;
import com.pm.taskapp.project.repository.ProjectMemberRepository;
import com.pm.taskapp.task.entity.Issue;
import com.pm.taskapp.task.exception.IssueNotFoundException;
import com.pm.taskapp.task.repository.IssueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentResponseDTO createComment(UUID issueId, CommentCreateRequestDTO request, UUID currentUserId) {
        log.info("Creating comment on issue: {} by user: {}", issueId, currentUserId);
        // 1. Validate current user exists
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with Id: " + currentUserId));

        // 2. Validate issue exists
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> IssueNotFoundException.byId(issueId));

        // 3. Validate user is a project member (authorization)
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(issue.getProject().getId(), currentUserId)) {
            throw CommentAccessDeniedException.notProjectMember();
        }

        // 4. Validate comment content is not empty
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw EmptyCommentContentException.empty();
        }
        Comment comment = Comment.builder()
                .content(request.getContent().trim())
                .author(currentUser)
                .issue(issue)
                .build();

        comment = commentRepository.save(comment);

        log.debug("Comment created successfully with ID: {}", comment.getId());

        return commentMapper.toResponseDTO(comment);
    }

    @Override
    public CommentResponseDTO updateComment(UUID commentId, CommentUpdateRequestDTO request, UUID currentUserId) {
        log.info("Updating comment: {} by user: {}", commentId, currentUserId);

        Comment comment = commentRepository.findByIdWithDetails(commentId)
                .orElseThrow(() -> CommentNotFoundException.byId(commentId));

        // 4. Check if current user is the author of the comment
        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw CommentAccessDeniedException.notAuthor();
        }
        // 5. Validate comment content is not empty
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw EmptyCommentContentException.empty();
        }
        comment.setContent(request.getContent().trim());
        comment = commentRepository.save(comment);
        log.debug("Comment updated successfully with ID: {}", comment.getId());

        return commentMapper.toResponseDTO(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponseDTO getCommentById(UUID commentId, UUID currentUserId) {
        log.debug("Fetching comment by ID: {} for user: {}", commentId, currentUserId);

        Comment comment = commentRepository.findByIdWithDetails(commentId)
                .orElseThrow(() -> CommentNotFoundException.byId(commentId));

        if (!projectMemberRepository.existsByProject_IdAndUser_Id(comment.getIssue().getProject().getId(),
                currentUserId)) {
            throw CommentAccessDeniedException.notProjectMember();
        }
        log.info("Comment fetched successfully with ID: {}", comment.getId());
        return commentMapper.toResponseDTO(comment);
    }

    @Override
    public void deleteComment(UUID commentId, UUID currentUserId) {
        log.info("Deleting comment: {} by user: {}", commentId, currentUserId);

        Comment comment = commentRepository.findByIdWithDetails(commentId)
                .orElseThrow(() -> CommentNotFoundException.byId(commentId));
        // 4. Check if current user is the author of the comment
        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw CommentAccessDeniedException.notAuthor();
        }
        if (comment.getIsDeleted()) {
            log.warn("Comment with ID: {} is already deleted", comment.getId());
            throw CommentAlreadyDeletedException.byId(commentId);
        }

        comment.setIsDeleted(true);
        comment.setDeletedAt(Instant.now());
        commentRepository.save(comment);
        log.info("Comment deleted successfully with ID: {}", comment.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentSummaryDTO> getIssueComments(UUID issueId, UUID currentUserId, Pageable pageable) {
        log.info("Fetching comments for issue: {} by user: {}", issueId, currentUserId);
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> IssueNotFoundException.byId(issueId));

        // 3. Validate user is a project member (authorization)
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(issue.getProject().getId(), currentUserId)) {
            throw CommentAccessDeniedException.notProjectMember();
        }
        Page<Comment> comments = commentRepository.findByIssue(issueId, pageable);
        log.debug("Fetched {} comments for issue: {}", comments.getTotalElements(), issueId);
        return comments.map(commentMapper::toSummaryDTO);
    }

    @Override
    public long countIssueComments(UUID issueId, UUID currentUserId) {
        log.info("Counting comments for issue: {} by user: {}", issueId, currentUserId);

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> IssueNotFoundException.byId(issueId));
        // 3. Validate user is a project member (authorization)
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(issue.getProject().getId(), currentUserId)) {
            throw CommentAccessDeniedException.notProjectMember();
        }
        long count = commentRepository.countByIssue(issueId);
        log.debug("Counted {} comments for issue: {}", count, issueId);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentSummaryDTO> getUserComments(UUID authorId, UUID currentUserId, Pageable pageable) {
        log.info("Fetching comments by author: {} for user: {}", authorId, currentUserId);

        // Validate author exists
        userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + authorId));

        Page<Comment> comments = commentRepository.findByAuthor(authorId, pageable);

        log.info("Fetched {} comments by author: {}", comments.getTotalElements(), authorId);

        return comments.map(commentMapper::toSummaryDTO);
    }

}
