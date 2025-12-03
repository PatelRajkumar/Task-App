package com.pm.taskapp.project.service.impl;

import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.auth.repository.UserRepository;
import com.pm.taskapp.project.dto.request.ProjectCreateRequestDTO;
import com.pm.taskapp.project.dto.request.ProjectUpdateRequestDTO;
import com.pm.taskapp.project.dto.response.ProjectResponseDTO;
import com.pm.taskapp.project.entity.Project;
import com.pm.taskapp.project.entity.ProjectIssueCounter;
import com.pm.taskapp.project.entity.ProjectMember;
import com.pm.taskapp.project.enums.ProjectRole;
import com.pm.taskapp.project.enums.ProjectVisibility;
import com.pm.taskapp.project.exception.ProjectAccessDeniedException;
import com.pm.taskapp.project.exception.ProjectNotFoundException;
import com.pm.taskapp.project.mapper.ProjectMapper;
import com.pm.taskapp.project.repository.ProjectIssueCounterRepository;
import com.pm.taskapp.project.repository.ProjectMemberRepository;
import com.pm.taskapp.project.repository.ProjectRepository;
import com.pm.taskapp.project.service.ProjectMemberService;
import com.pm.taskapp.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of ProjectService for project management operations.
 * Follows the same pattern as AuthenticationServiceImpl and UserServiceImpl.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectIssueCounterRepository projectIssueCounterRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final ProjectMemberService projectMemberService;

    @Override
    public ProjectResponseDTO createProject(ProjectCreateRequestDTO request, UUID creatorId) {
        log.info("Creating project '{}' for user: {}", request.getName(), creatorId);

        // Find creator
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found with ID: " + creatorId));

        // Generate unique project key
        String projectKey = projectRepository.getNextProjectKey();
        log.debug("Generated project key: {}", projectKey);

        // Create project
        Project project = Project.builder()
                .key(projectKey)
                .name(request.getName())
                .description(request.getDescription())
                .visibility(request.getVisibility() != null ? request.getVisibility() : ProjectVisibility.PRIVATE)
                .createdBy(creator)
                .isArchived(false)
                .build();

        // Save project
        project = projectRepository.save(project);
        log.debug("Project created with ID: {}", project.getId());

        // Create issue counter for project
        ProjectIssueCounter counter = ProjectIssueCounter.builder()
                .project(project)
                .lastNumber(0)
                .build();
        projectIssueCounterRepository.save(counter);
        log.debug("Issue counter created for project: {}", project.getKey());

        // Add creator as OWNER
        ProjectMember ownerMember = ProjectMember.builder()
                .project(project)
                .user(creator)
                .role(ProjectRole.OWNER)
                .build();
        projectMemberRepository.save(ownerMember);
        log.debug("Creator added as OWNER of project: {}", project.getKey());

        log.info("Project created successfully: {} ({})", project.getName(), project.getKey());

        // Return response with user's role
        return projectMapper.toResponseDTOWithUserRole(project, ProjectRole.OWNER);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(UUID projectId, UUID currentUserId) {
        log.debug("Getting project by ID: {} for user: {}", projectId, currentUserId);

        // Find project
        Project project = findProjectOrThrow(projectId);

        // Check access
        validateUserAccess(project, currentUserId);

        // Get user's role
        ProjectRole userRole = projectMemberService.getUserRole(projectId, currentUserId);

        // Get counts
        Integer memberCount = (int) projectMemberRepository.countByProject_Id(projectId);
        Integer issueCount = projectIssueCounterRepository.getCurrentCounterValue(projectId).orElse(0);

        // Build response
        ProjectResponseDTO response = projectMapper.toResponseDTOWithCounts(project, memberCount, issueCount);
        if (userRole != null) {
            response.setCurrentUserRole(userRole.name());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectByKey(String projectKey, UUID currentUserId) {
        log.debug("Getting project by key: {} for user: {}", projectKey, currentUserId);

        // Find project
        Project project = projectRepository.findByKey(projectKey)
                .orElseThrow(() -> ProjectNotFoundException.byKey(projectKey));

        // Check access
        validateUserAccess(project, currentUserId);

        // Get user's role
        ProjectRole userRole = projectMemberService.getUserRole(project.getId(), currentUserId);

        // Get counts
        Integer memberCount = (int) projectMemberRepository.countByProject_Id(project.getId());
        Integer issueCount = projectIssueCounterRepository.getCurrentCounterValue(project.getId()).orElse(0);

        // Build response
        ProjectResponseDTO response = projectMapper.toResponseDTOWithCounts(project, memberCount, issueCount);
        if (userRole != null) {
            response.setCurrentUserRole(userRole.name());
        }

        return response;
    }

    @Override
    public ProjectResponseDTO updateProject(UUID projectId, ProjectUpdateRequestDTO request, UUID currentUserId) {
        log.info("Updating project: {} by user: {}", projectId, currentUserId);

        // Find project
        Project project = findProjectOrThrow(projectId);

        // Check permission (only OWNER and ADMIN can update)
        if (!projectMemberService.isOwnerOrAdmin(projectId, currentUserId)) {
            throw ProjectAccessDeniedException.insufficientRole();
        }

        // Update fields
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getVisibility() != null) {
            project.setVisibility(request.getVisibility());
        }

        // Save
        project = projectRepository.save(project);
        log.info("Project updated successfully: {}", project.getKey());

        // Get user's role
        ProjectRole userRole = projectMemberService.getUserRole(projectId, currentUserId);

        return projectMapper.toResponseDTOWithUserRole(project, userRole);
    }

    @Override
    public void archiveProject(UUID projectId, UUID currentUserId) {
        log.info("Archiving project: {} by user: {}", projectId, currentUserId);

        // Find project
        Project project = findProjectOrThrow(projectId);

        // Check permission (only OWNER can archive)
        if (!projectMemberService.isOwner(projectId, currentUserId)) {
            throw ProjectAccessDeniedException.forAction("archive");
        }

        // Archive
        project.archive();
        projectRepository.save(project);

        log.info("Project archived successfully: {}", project.getKey());
    }

    @Override
    public void restoreProject(UUID projectId, UUID currentUserId) {
        log.info("Restoring project: {} by user: {}", projectId, currentUserId);

        // Find project
        Project project = findProjectOrThrow(projectId);

        // Check permission (only OWNER can restore)
        if (!projectMemberService.isOwner(projectId, currentUserId)) {
            throw ProjectAccessDeniedException.forAction("restore");
        }

        // Restore
        project.restore();
        projectRepository.save(project);

        log.info("Project restored successfully: {}", project.getKey());
    }

    @Override
    public void deleteProject(UUID projectId, UUID currentUserId) {
        log.info("Deleting project: {} by user: {}", projectId, currentUserId);

        // Find project
        Project project = findProjectOrThrow(projectId);

        // Check permission (only OWNER can delete)
        if (!projectMemberService.isOwner(projectId, currentUserId)) {
            throw ProjectAccessDeniedException.forAction("delete");
        }

        // Delete (cascade will handle members and counter)
        projectRepository.delete(project);

        log.info("Project deleted successfully: {}", project.getKey());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDTO> getUserProjects(UUID userId, Pageable pageable) {
        log.debug("Getting projects for user: {}", userId);

        Page<Project> projects = projectRepository.findUserProjects(userId, pageable);

        return projects.map(project -> {
            ProjectRole userRole = projectMemberService.getUserRole(project.getId(), userId);
            return projectMapper.toResponseDTOWithUserRole(project, userRole);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDTO> getArchivedProjects(UUID userId, Pageable pageable) {
        log.debug("Getting archived projects for user: {}", userId);

        Page<Project> projects = projectRepository.findArchivedProjectsOwnedBy(userId, pageable);

        return projects.map(project -> projectMapper.toResponseDTOWithUserRole(project, ProjectRole.OWNER));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDTO> searchProjects(String searchTerm, UUID currentUserId, Pageable pageable) {
        log.debug("Searching projects with term: {} for user: {}", searchTerm, currentUserId);

        Page<Project> projects = projectRepository.searchProjects(searchTerm, currentUserId, pageable);

        return projects.map(project -> {
            ProjectRole userRole = projectMemberService.getUserRole(project.getId(), currentUserId);
            return projectMapper.toResponseDTOWithUserRole(project, userRole);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDTO> getPublicProjects(Pageable pageable) {
        log.debug("Getting public projects");

        Page<Project> projects = projectRepository.findPublicProjects(pageable);

        return projects.map(projectMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserAccessProject(UUID projectId, UUID userId) {
        Project project = findProjectOrThrow(projectId);

        // Public projects are accessible to everyone
        if (project.isPublic() && !project.getIsArchived()) {
            return true;
        }

        // Private or archived projects require membership
        boolean isMember = projectMemberService.isMember(projectId, userId);
        
        // Archived projects only accessible to owners
        if (project.getIsArchived()) {
            return isMember && projectMemberService.isOwner(projectId, userId);
        }

        return isMember;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByKey(String projectKey) {
        return projectRepository.existsByKey(projectKey);
    }

    // ========== Helper Methods ==========

    /**
     * Find project by ID or throw exception.
     */
    private Project findProjectOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> ProjectNotFoundException.byId(projectId));
    }

    /**
     * Validate user has access to view project.
     */
    private void validateUserAccess(Project project, UUID userId) {
        // Public projects are accessible to everyone
        if (project.isPublic() && !project.getIsArchived()) {
            return;
        }

        // Private or archived projects require membership
        boolean isMember = projectMemberService.isMember(project.getId(), userId);
        if (!isMember) {
            throw ProjectAccessDeniedException.notMember();
        }

        // Archived projects only accessible to owners
        if (project.getIsArchived() && !projectMemberService.isOwner(project.getId(), userId)) {
            throw ProjectAccessDeniedException.archivedProject();
        }
    }
}