package com.pm.taskapp.project.service;

import com.pm.taskapp.project.dto.request.ProjectCreateRequestDTO;
import com.pm.taskapp.project.dto.request.ProjectUpdateRequestDTO;
import com.pm.taskapp.project.dto.response.ProjectResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for project management operations.
 * Defines business logic for project CRUD and related operations.
 * 
 * @since 1.0.0
 */
public interface ProjectService {

    /**
     * Create a new project.
     * Creator automatically becomes the project OWNER.
     * 
     * @param request Project creation data
     * @param creatorId ID of the user creating the project
     * @return Created project response
     */
    ProjectResponseDTO createProject(ProjectCreateRequestDTO request, UUID creatorId);

    /**
     * Get project by ID.
     * Checks access permissions before returning.
     * 
     * @param projectId Project ID
     * @param currentUserId Current user ID for access check
     * @return Project response with user's role
     */
    ProjectResponseDTO getProjectById(UUID projectId, UUID currentUserId);

    /**
     * Get project by key.
     * Checks access permissions before returning.
     * 
     * @param projectKey Project key (e.g., PROJ-1)
     * @param currentUserId Current user ID for access check
     * @return Project response with user's role
     */
    ProjectResponseDTO getProjectByKey(String projectKey, UUID currentUserId);

    /**
     * Update project details.
     * Only OWNER and ADMIN can update.
     * 
     * @param projectId Project ID
     * @param request Update data
     * @param currentUserId Current user ID for permission check
     * @return Updated project response
     */
    ProjectResponseDTO updateProject(UUID projectId, ProjectUpdateRequestDTO request, UUID currentUserId);

    /**
     * Archive project.
     * Only OWNER can archive.
     * 
     * @param projectId Project ID
     * @param currentUserId Current user ID for permission check
     */
    void archiveProject(UUID projectId, UUID currentUserId);

    /**
     * Restore archived project.
     * Only OWNER can restore.
     * 
     * @param projectId Project ID
     * @param currentUserId Current user ID for permission check
     */
    void restoreProject(UUID projectId, UUID currentUserId);

    /**
     * Delete project permanently.
     * Only OWNER can delete.
     * 
     * @param projectId Project ID
     * @param currentUserId Current user ID for permission check
     */
    void deleteProject(UUID projectId, UUID currentUserId);

    /**
     * Get all projects user is a member of.
     * Excludes archived projects unless user is OWNER.
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of projects
     */
    Page<ProjectResponseDTO> getUserProjects(UUID userId, Pageable pageable);

    /**
     * Get archived projects where user is OWNER.
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of archived projects
     */
    Page<ProjectResponseDTO> getArchivedProjects(UUID userId, Pageable pageable);

    /**
     * Search projects user can access.
     * 
     * @param searchTerm Search term (matches name or key)
     * @param currentUserId Current user ID for access control
     * @param pageable Pagination parameters
     * @return Page of matching projects
     */
    Page<ProjectResponseDTO> searchProjects(String searchTerm, UUID currentUserId, Pageable pageable);

    /**
     * Get all public active projects.
     * No authentication required.
     * 
     * @param pageable Pagination parameters
     * @return Page of public projects
     */
    Page<ProjectResponseDTO> getPublicProjects(Pageable pageable);

    /**
     * Check if user has access to view project.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return true if user can view project
     */
    boolean canUserAccessProject(UUID projectId, UUID userId);

    /**
     * Check if project exists by key.
     * 
     * @param projectKey Project key
     * @return true if project exists
     */
    boolean existsByKey(String projectKey);
}