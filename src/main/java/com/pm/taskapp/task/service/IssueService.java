package com.pm.taskapp.task.service;

import com.pm.taskapp.task.dto.*;
import com.pm.taskapp.task.enums.IssuePriority;
import com.pm.taskapp.task.enums.IssueStatus;
import com.pm.taskapp.task.enums.IssueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service interface for issue/task management.
 * Handles CRUD operations, status workflows, assignments, search, and history
 * tracking.
 */
public interface IssueService {

        // ========== CRUD Operations ==========

        IssueResponseDTO createIssue(UUID projectId, IssueCreateRequestDTO requestDTO, UUID currentUserId);

        IssueResponseDTO getIssueById(UUID issueId, UUID currentUserId);

        IssueResponseDTO getIssueByKey(String key, UUID currentUserId);

        IssueResponseDTO updateIssue(UUID issueId, UUID projectId, IssueUpdateRequestDTO requestDTO,
                        UUID currentUserId);

        void deleteIssue(UUID issueId, UUID projectId, UUID currentUserId);

        // ========== Status Management ==========

        IssueResponseDTO updateIssueStatus(UUID issueId, UUID projectId, IssueUpdateStatusRequestDTO requestDTO,
                        UUID currentUserId);

        // ========== Assignment Management ==========

        IssueResponseDTO assignIssue(UUID issueId, UUID projectId, IssueAssignRequestDTO requestDTO, UUID currentUserId);

        // ========== List & Search Operations ==========

        Page<IssueSummaryDTO> getProjectIssues(UUID projectId, Pageable pageable,UUID currentUserId);

        Page<IssueSummaryDTO> searchIssues(UUID projectId, String searchTerm, Pageable pageable,UUID currentUserId);

        Page<IssueSummaryDTO> filterIssues(
                        UUID projectId,
                        IssueStatus status,
                        IssueType type,
                        IssuePriority priority,
                        UUID assigneeId,
                        UUID reporterId,
                        UUID currentUserId,
                        Pageable pageable);

        // ========== User-Specific Queries ==========

        Page<IssueSummaryDTO> getMyAssignedIssues(UUID currentUserId, Pageable pageable);

        Page<IssueSummaryDTO> getMyReportedIssues(UUID currentUserId, Pageable pageable);

        Page<IssueSummaryDTO> getUnassignedIssues(UUID projectId, UUID currentUserId, Pageable pageable);

        // ========== Date-Based Queries ==========

        Page<IssueSummaryDTO> getOverdueIssues(UUID projectId, UUID currentUserId,LocalDate asOfDate, Pageable pageable);

        Page<IssueSummaryDTO> getRecentlyUpdatedIssues(UUID projectId, UUID currentUserId, Pageable pageable);

        // ========== History & Audit ==========

        Page<IssueHistoryResponseDTO> getIssueHistory(UUID issueId, UUID currentUserId, Pageable pageable);

        Page<IssueHistoryResponseDTO> getProjectActivityHistory(UUID projectId, UUID currentUserId, Pageable pageable);

        // ========== Statistics & Counts ==========

        long countProjectIssues(UUID projectId);

        long countMyAssignedIssues(UUID currentUserId);
}