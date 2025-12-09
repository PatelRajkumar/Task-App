package com.pm.taskapp.task.service;

import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.auth.repository.UserRepository;
import com.pm.taskapp.project.entity.Project;
import com.pm.taskapp.project.entity.ProjectIssueCounter;
import com.pm.taskapp.project.entity.ProjectMember;
import com.pm.taskapp.project.exception.ProjectMemberNotFoundException;
import com.pm.taskapp.project.exception.ProjectNotFoundException;
import com.pm.taskapp.project.repository.ProjectIssueCounterRepository;
import com.pm.taskapp.project.repository.ProjectMemberRepository;
import com.pm.taskapp.project.repository.ProjectRepository;
import com.pm.taskapp.task.dto.*;
import com.pm.taskapp.task.entity.Issue;
import com.pm.taskapp.task.entity.IssueHistory;
import com.pm.taskapp.task.enums.IssuePriority;
import com.pm.taskapp.task.enums.IssueStatus;
import com.pm.taskapp.task.enums.IssueType;
import com.pm.taskapp.task.exception.*;
import com.pm.taskapp.task.mapper.IssueMapper;
import com.pm.taskapp.task.repository.IssueHistoryRepository;
import com.pm.taskapp.task.repository.IssueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IssueServiceImpl implements IssueService {

    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final IssueMapper issueMapper;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectIssueCounterRepository projectIssueCounterRepository;
    private final IssueHistoryRepository issueHistoryRepository;

    @Override
    public IssueResponseDTO createIssue(UUID projectId, IssueCreateRequestDTO requestDTO, UUID currentUserId) {
        log.info("Creating issue '{}' in project: {} by user: {}", requestDTO.getTitle(), projectId, currentUserId);

        // 1. Validate current user exists
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with Id: " + currentUserId));

        // 2. Validate project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> ProjectNotFoundException.byId(projectId));

        // 3. Validate user is a project member (authorization)
        ProjectMember member = projectMemberRepository.findByProject_IdAndUser_Id(projectId, currentUserId)
                .orElseThrow(() -> ProjectMemberNotFoundException.notMember());

        // 4. Validate due date (if provided)
        if (requestDTO.getDueDate() != null && requestDTO.getDueDate().isBefore(LocalDate.now())) {
            throw InvalidDueDateException.pastDate(requestDTO.getDueDate());
        }

        User assignee = null;
        if (requestDTO.getAssigneeId() != null) {
            assignee = userRepository.findById(requestDTO.getAssigneeId()).orElseThrow(
                    () -> new IllegalArgumentException("Assignee not found with Id: " + requestDTO.getAssigneeId()));

            if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, requestDTO.getAssigneeId())) {
                throw new IllegalArgumentException(
                        "Assignee must be a project member. User " + requestDTO.getAssigneeId() +
                                " is not a member of project " + projectId);
            }
        }

        ProjectIssueCounter counter = projectIssueCounterRepository.findByProjectIdForUpdate(projectId)
                .orElseGet(() -> {
                    log.warn("Counter not found for project {}, creating new one. " +
                            "This should have been created during project creation.", projectId);
                    ProjectIssueCounter newCounter = ProjectIssueCounter.builder().project(project).lastNumber(0)
                            .build();
                    return projectIssueCounterRepository.save(newCounter);
                });
        Integer sequentialNumber = counter.incrementAndGet();
        projectIssueCounterRepository.save(counter);

        // 7. Generate issue key (e.g., PROJ-123)
        String issueKey = project.getKey() + "-" + sequentialNumber;
        log.debug("Generated issue key: {}", issueKey);

        // generate issue key
        Issue issue = Issue.builder()
                .key(issueKey)
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .project(project)
                .status(IssueStatus.TODO)
                .sequentialNumber(sequentialNumber)
                .type(requestDTO.getType())
                .dueDate(requestDTO.getDueDate())
                .priority(requestDTO.getPriority())
                .reporter(currentUser)
                .assignee(assignee)
                .build();
        issue = issueRepository.save(issue);

        log.info("Issue created successfully with key: {}", issue.getKey());

        IssueHistory createHistory = IssueHistory.builder()
                .issue(issue)
                .field("status")
                .oldValue(null)
                .newValue(IssueStatus.TODO.name())
                .changedBy(currentUser)
                .changedAt(Instant.now())
                .build();

        issueHistoryRepository.save(createHistory);

        // 10. Create assignment history if issue is assigned during creation
        if (assignee != null) {
            IssueHistory assignmentHistory = IssueHistory.builder()
                    .issue(issue)
                    .field("assignee")
                    .oldValue(null)
                    .newValue(assignee.getName())
                    .changedBy(currentUser)
                    .changedAt(Instant.now())
                    .build();
            issueHistoryRepository.save(assignmentHistory);

            // TODO: Send assignment email notification to assignee
            log.debug("TODO: Send assignment email to: {}", assignee.getEmail());
        }

        return issueMapper.toResponseDTO(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponseDTO getIssueById(UUID issueId, UUID currentUserId) {

        log.debug("Getting issue by ID: {} for user: {}", issueId, currentUserId);

        // 1. Find issue with relationships loaded (avoid N+1 queries)
        Issue issue = issueRepository.findByIdWithDetails(issueId)
                .orElseThrow(() -> IssueNotFoundException.byId(issueId));

        if (!projectMemberRepository.existsByProject_IdAndUser_Id(issue.getProject().getId(), currentUserId)) {
            throw new IssueAccessDeniedException("You don't have access to this issue");
        }

        IssueResponseDTO responseDTO = issueMapper.toResponseDTO(issue);
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponseDTO getIssueByKey(String key, UUID currentUserId) {
        log.debug("Getting issue by Key: {} for user: {}", key, currentUserId);

        Issue issue = issueRepository.findByKeyWithDetails(key).orElseThrow(() -> IssueNotFoundException.byKey(key));

        if (!projectMemberRepository.existsByProject_IdAndUser_Id(issue.getProject().getId(), currentUserId)) {
            throw new IssueAccessDeniedException("You don't have access to this issue");
        }

        IssueResponseDTO responseDTO = issueMapper.toResponseDTO(issue);
        return responseDTO;
    }

    @Override
    public IssueResponseDTO updateIssue(UUID issueId, UUID projectId, IssueUpdateRequestDTO requestDTO,
            UUID currentUserId) {
        log.info("Updating issue: {} in project: {} by user: {}", issueId, projectId, currentUserId);

        // 1. Validate at least one field is being updated
        if (!requestDTO.hasAtLeastOneField()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }

        // 2. fetch issue
        Issue issue = issueRepository.findByIdWithDetails(issueId)
                .orElseThrow(() -> IssueNotFoundException.byId(issueId));

        // 3. Get current user
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUserId));

        if (!canEditIssue(issue, currentUserId, projectId)) {
            throw new IssueAccessDeniedException("You don't have access to edit issue");
        }

        // Validate due date (if provided)
        if (requestDTO.getDueDate() != null && requestDTO.getDueDate().isBefore(LocalDate.now())) {
            throw InvalidDueDateException.pastDate(requestDTO.getDueDate());
        }

        User newAssignee = null;
        if (requestDTO.getAssigneeId() != null) {
            newAssignee = userRepository.findById(requestDTO.getAssigneeId()).orElseThrow(
                    () -> new IllegalArgumentException("Assignee not found with Id: " + requestDTO.getAssigneeId()));

            if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, requestDTO.getAssigneeId())) {
                throw new IllegalArgumentException(
                        "Assignee must be a project member. User " + requestDTO.getAssigneeId() +
                                " is not a member of project " + projectId);
            }
        }

        boolean hasChanges = false;

        // Update fields
        // Update title (tracked)
        if (requestDTO.getTitle() != null && !requestDTO.getTitle().equals(issue.getTitle())) {
            String oldTitle = issue.getTitle();
            issue.setTitle(requestDTO.getTitle());
            createHistoryRecord(issue, "title", oldTitle, requestDTO.getTitle(), currentUser);
            hasChanges = true;
        }

        // Update priority (tracked)
        if (requestDTO.getPriority() != null && !requestDTO.getPriority().equals(issue.getPriority())) {
            IssuePriority oldPriority = issue.getPriority();
            issue.setPriority(requestDTO.getPriority());
            createHistoryRecord(issue, "priority", oldPriority.name(), requestDTO.getPriority().name(), currentUser);
            hasChanges = true;
        }

        // Update assignee (tracked)
        if (requestDTO.getAssigneeId() != null) {
            User oldAssignee = issue.getAssignee();
            String oldAssigneeName = oldAssignee != null ? oldAssignee.getName() : null;
            String newAssigneeName = newAssignee != null ? newAssignee.getName() : null;

            if (!isSameAssignee(oldAssignee, newAssignee)) {
                issue.setAssignee(newAssignee);
                createHistoryRecord(issue, "assignee", oldAssigneeName, newAssigneeName, currentUser);
                hasChanges = true;

                // Send email notification to new assignee
                if (newAssignee != null) {
                    // TODO: Send assignment email
                    log.debug("TODO: Send assignment email to: {}", newAssignee.getEmail());
                }
            }
        }
        if (requestDTO.getDescription() != null) {

            issue.setDescription(requestDTO.getDescription());
            hasChanges = true;
        }
        if (requestDTO.getDueDate() != null) {

            issue.setDueDate(requestDTO.getDueDate());
            hasChanges = true;
        }
        if (hasChanges) {
            issue = issueRepository.save(issue);
            log.info("Issue {} updated successfully", issue.getKey());
        } else {
            log.debug("No changes detected for issue {}", issue.getKey());
        }
        return issueMapper.toResponseDTO(issue);
    }

    @Override
    @Transactional
    public void deleteIssue(UUID issueId, UUID projectId, UUID currentUserId) {
        log.info("Deleting issue: {} by user: {}", issueId, currentUserId);

        // 1. Fetch issue
        Issue issue = issueRepository.findByIdWithDetails(issueId)
                .orElseThrow(() -> IssueNotFoundException.byId(issueId));

        // 2. Check if already deleted
        if (issue.isDeleted()) {
            throw IssueAlreadyDeletedException.byKey(issue.getKey());
        }

        // 3. Check authorization
        if (!canEditIssue(issue, currentUserId, projectId)) {
            throw new IssueAccessDeniedException("You don't have access to edit issue");
        }

        // 4. Get current user (for deletedBy)
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUserId));

        // 5. Soft delete
        issue.setIsDeleted(true);
        issue.setDeletedAt(Instant.now());
        issue.setDeletedBy(currentUser);

        createHistoryRecord(issue, "deleted", "false", "true", currentUser);

        issueRepository.save(issue);

        log.info("Issue deleted successfully: {}", issue.getKey());
    }
    @Override
    public IssueResponseDTO updateIssueStatus(UUID issueId, UUID projectId, IssueUpdateStatusRequestDTO requestDTO,
            UUID currentUserId) {
        log.info("Updating issue status: {} by user: {}", issueId, currentUserId);

        Issue issue = issueRepository.findByIdWithDetails(issueId)
                .orElseThrow(() -> IssueNotFoundException.byId(issueId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUserId));

        if (!canEditIssue(issue, currentUserId, projectId)) {
            throw new IssueAccessDeniedException("You don't have access to edit issue");
        }

        IssueStatus oldStatus = issue.getStatus();
        IssueStatus newStatus = requestDTO.getNewStatus();

        if (oldStatus.equals(newStatus)) {
            log.debug("Status unchanged for issue {}: already {}", issue.getKey(), oldStatus);
            return issueMapper.toResponseDTO(issue);
        }

        try {
            oldStatus.validateTransition(newStatus);
        } catch (InvalidStatusTransitionException e) {
            log.warn("Invalid status transition for issue {}: {} -> {}",
                    issue.getKey(), oldStatus, newStatus);
            throw e;
        }

        issue.setStatus(newStatus);

        if (newStatus == IssueStatus.DONE) {
            issue.setResolvedAt(Instant.now());
            log.debug("Issue {} marked as resolved", issue.getKey());
        } else if (oldStatus == IssueStatus.DONE) {
            // Reopening issue - clear resolved timestamp
            issue.setResolvedAt(null);
            log.debug("Issue {} reopened, cleared resolved timestamp", issue.getKey());
        }

        createHistoryRecord(issue, "status", oldStatus.name(), newStatus.name(), currentUser);

        // 10. Save issue
        issue = issueRepository.save(issue);
        log.info("Issue {} status updated: {} -> {}", issue.getKey(), oldStatus, newStatus);

        // 11. Send email notifications when marked as DONE
        if (newStatus == IssueStatus.DONE) {
            // Notify reporter
            log.debug("TODO: Send completion email to reporter: {}", issue.getReporter().getEmail());

            // Notify assignee (if assigned and different from reporter)
            if (issue.getAssignee() != null &&
                    !issue.getAssignee().getId().equals(issue.getReporter().getId())) {
                log.debug("TODO: Send completion email to assignee: {}", issue.getAssignee().getEmail());
            }
        }
        return issueMapper.toResponseDTO(issue);
    }

    @Override
    @Transactional
    public IssueResponseDTO assignIssue(UUID issueId, UUID projectId, IssueAssignRequestDTO requestDTO, UUID currentUserId) {
        UUID assigneeId = requestDTO.getAssigneeId();

        if (assigneeId == null) {
            log.info("Unassigning issue: {} by user: {}", issueId, currentUserId);
        } else {
            log.info("Assigning issue: {} to user: {} by user: {}", issueId, assigneeId, currentUserId);
        }

        // 1. Fetch issue
        Issue issue = issueRepository.findByIdWithDetails(issueId)
                .orElseThrow(() -> IssueNotFoundException.byId(issueId));

        // 2. Check if already deleted
        if (issue.isDeleted()) {
            throw IssueAlreadyDeletedException.byKey(issue.getKey());
        }

        // 3. Check authorization
        if (!canEditIssue(issue, currentUserId, projectId)) {
            throw new IssueAccessDeniedException("You don't have access to edit issue");
        }

        // 4. Get current user (for history tracking)
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUserId));

        User oldAssignee = issue.getAssignee();

        // 5. Handle UNASSIGNMENT (assigneeId is null)
        if (assigneeId == null) {
            if (oldAssignee == null) {
                log.debug("Issue {} is already unassigned", issue.getKey());
                return issueMapper.toResponseDTO(issue);
            }

            // Unassign
            issue.setAssignee(null);
            createHistoryRecord(
                    issue,
                    "assignee",
                    oldAssignee.getName(),
                    null,
                    currentUser
            );

            issue = issueRepository.save(issue);
            log.info("Issue {} unassigned successfully", issue.getKey());

            return issueMapper.toResponseDTO(issue);
        }

        // 6. Handle ASSIGNMENT (assigneeId is provided)

        // Fetch and validate new assignee
        User newAssignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new IllegalArgumentException("Assignee not found with ID: " + assigneeId));

        // Validate assignee is project member
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, assigneeId)) {
            throw new IllegalArgumentException(
                    "Assignee must be a project member. User " + assigneeId +
                            " is not a member of project " + projectId);
        }

        // Check if already assigned to same user (early exit)
        if (isSameAssignee(oldAssignee, newAssignee)) {
            log.debug("Issue {} already assigned to user {}", issue.getKey(), assigneeId);
            return issueMapper.toResponseDTO(issue);
        }

        // Update assignee
        issue.setAssignee(newAssignee);

        // Create history record
        createHistoryRecord(
                issue,
                "assignee",
                oldAssignee != null ? oldAssignee.getName() : null,
                newAssignee.getName(),
                currentUser
        );

        // Save
        issue = issueRepository.save(issue);

        // Send email notification
        log.debug("TODO: Send assignment email to: {}", newAssignee.getEmail());

        log.info("Issue {} assigned successfully to {}", issue.getKey(), newAssignee.getEmail());

        return issueMapper.toResponseDTO(issue);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<IssueSummaryDTO> getProjectIssues(UUID projectId, Pageable pageable,UUID currentUserId) {
        log.info("Getting all issues of project: {} for user: {}", projectId, currentUserId);
        // 1. Validate project exists
        projectRepository.findById(projectId)
                .orElseThrow(() -> ProjectNotFoundException.byId(projectId));

        // 2. Check user is project member (authorization)
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, currentUserId)) {
            throw new IssueAccessDeniedException("You don't have access to this project's issues");
        }


        Page<Issue> issues = issueRepository.findByProject(projectId,pageable);
        return issues.map(issue -> issueMapper.toSummaryDTO(issue));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IssueSummaryDTO> searchIssues(UUID projectId, String searchTerm,Pageable pageable, UUID currentUserId
            ) {
        log.info("Search issue in project: {} for user: {}",projectId,currentUserId);

         projectRepository.findById(projectId)
                .orElseThrow(() -> ProjectNotFoundException.byId(projectId));

        // 2. Check user is project member (authorization)
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, currentUserId)) {
            throw new IssueAccessDeniedException("You don't have access to this project's issues");
        }
        // 3. Handle empty search term (return all issues)
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            log.debug("Empty search term, returning all issues");
            return getProjectIssues(projectId, pageable, currentUserId);
        }
        String normalizedSearchTerm = searchTerm.trim();
        Page<Issue> issues = issueRepository.searchInProject(projectId,normalizedSearchTerm,pageable);
        return issues.map(issue -> issueMapper.toSummaryDTO(issue));
    }

    @Override
    public Page<IssueSummaryDTO> filterIssues(UUID projectId, IssueStatus status, IssueType type,
            IssuePriority priority, UUID assigneeId, UUID currentUserId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<IssueSummaryDTO> getMyAssignedIssues(UUID currentUserId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<IssueSummaryDTO> getMyReportedIssues(UUID currentUserId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<IssueSummaryDTO> getProjectIssuesByAssignee(UUID projectId, UUID assigneeId, UUID currentUserId,
            Pageable pageable) {
        return null;
    }

    @Override
    public Page<IssueSummaryDTO> getUnassignedIssues(UUID projectId, UUID currentUserId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<IssueSummaryDTO> getOverdueIssues(UUID projectId, UUID currentUserId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<IssueSummaryDTO> getRecentlyUpdatedIssues(UUID projectId, UUID currentUserId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<IssueHistoryResponseDTO> getIssueHistory(UUID issueId, UUID currentUserId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<IssueHistoryResponseDTO> getProjectActivityHistory(UUID projectId, UUID currentUserId,
            Pageable pageable) {
        return null;
    }

    @Override
    public long countProjectIssues(UUID projectId) {
        return 0;
    }

    @Override
    public long countMyAssignedIssues(UUID currentUserId) {
        return 0;
    }

    private boolean canEditIssue(Issue issue, UUID userId, UUID projectId) {
        // User is the reporter
        if (issue.getReporter().getId().equals(userId)) {
            return true;
        }

        // User is the assignee
        if (issue.getAssignee() != null && issue.getAssignee().getId().equals(userId)) {
            return true;
        }

        // User is project OWNER or ADMIN
        return projectMemberRepository.findByProject_IdAndUser_Id(projectId, userId)
                .map(member -> member.getRole().canEditProject()) // OWNER and ADMIN have this permission
                .orElse(false);
    }

    /**
     * Create a history record for tracked field changes.
     */
    private void createHistoryRecord(Issue issue, String field, String oldValue, String newValue, User changedBy) {
        IssueHistory history = IssueHistory.builder()
                .issue(issue)
                .field(field)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(changedBy)
                .changedAt(Instant.now())
                .build();
        issueHistoryRepository.save(history);
    }

    private boolean isSameAssignee(User oldAssignee, User newAssignee) {
        if (oldAssignee == null && newAssignee == null)
            return true;
        if (oldAssignee == null || newAssignee == null)
            return false;
        return oldAssignee.getId().equals(newAssignee.getId());
    }
}
