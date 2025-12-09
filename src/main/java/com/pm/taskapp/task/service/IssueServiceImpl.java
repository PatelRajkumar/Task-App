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
import com.pm.taskapp.task.exception.InvalidDueDateException;
import com.pm.taskapp.task.exception.IssueAccessDeniedException;
import com.pm.taskapp.task.exception.IssueNotFoundException;
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
                .orElseThrow(() -> new IssueNotFoundException(issueId));

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

        Issue issue = issueRepository.findByKeyWithDetails(key).orElseThrow(() -> new IssueNotFoundException(key));

        if (!projectMemberRepository.existsByProject_IdAndUser_Id(issue.getProject().getId(), currentUserId)) {
            throw new IssueAccessDeniedException("You don't have access to this issue");
        }

        IssueResponseDTO responseDTO = issueMapper.toResponseDTO(issue);
        return responseDTO;
    }

    @Override
    public IssueResponseDTO updateIssue(UUID issueId, IssueUpdateRequestDTO requestDTO, UUID currentUserId) {
        return null;
    }

    @Override
    public void deleteIssue(UUID issueId, UUID currentUserId) {

    }

    @Override
    public IssueResponseDTO updateIssueStatus(UUID issueId, IssueUpdateStatusRequestDTO requestDTO,
            UUID currentUserId) {
        return null;
    }

    @Override
    public IssueResponseDTO assignIssue(UUID issueId, UUID assigneeId, UUID currentUserId) {
        return null;
    }

    @Override
    public IssueResponseDTO unassignIssue(UUID issueId, UUID currentUserId) {
        return null;
    }

    @Override
    public Page<IssueSummaryDTO> getProjectIssues(UUID projectId, UUID currentUserId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<IssueSummaryDTO> searchIssues(UUID projectId, String searchTerm, UUID currentUserId,
            Pageable pageable) {
        return null;
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
}
