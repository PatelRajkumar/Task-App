package com.pm.taskapp.project.controller;

import com.pm.taskapp.auth.security.CurrentUser;
import com.pm.taskapp.auth.security.UserPrincipal;
import com.pm.taskapp.project.dto.request.ProjectCreateRequestDTO;
import com.pm.taskapp.project.dto.request.ProjectUpdateRequestDTO;
import com.pm.taskapp.project.dto.response.ProjectResponseDTO;
import com.pm.taskapp.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for project management operations.
 * Provides endpoints for project CRUD, archiving, and search.
 * 
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project Management", description = "Project management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Create a new project.
     * Creator automatically becomes the project OWNER.
     */
    @PostMapping
    @Operation(
        summary = "Create new project",
        description = "Create a new project. The creator automatically becomes the project OWNER."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Project created successfully",
            content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectCreateRequestDTO request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Creating project '{}' for user: {}", request.getName(), currentUser.getId());
        ProjectResponseDTO project = projectService.createProject(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    /**
     * Get project by ID.
     * Requires membership or public project.
     */
    @GetMapping("/{projectId}")
    @Operation(
        summary = "Get project by ID",
        description = "Get project details by ID. Requires membership or public project."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Project retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ProjectResponseDTO> getProjectById(
            @PathVariable @Parameter(description = "Project ID") UUID projectId,
            @CurrentUser UserPrincipal currentUser) {
        
        log.debug("Getting project by ID: {} for user: {}", projectId, currentUser.getId());
        ProjectResponseDTO project = projectService.getProjectById(projectId, currentUser.getId());
        return ResponseEntity.ok(project);
    }

    /**
     * Get project by key.
     * Requires membership or public project.
     */
    @GetMapping("/key/{projectKey}")
    @Operation(
        summary = "Get project by key",
        description = "Get project details by key (e.g., PROJ-1). Requires membership or public project."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Project retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ProjectResponseDTO> getProjectByKey(
            @PathVariable @Parameter(description = "Project key (e.g., PROJ-1)") String projectKey,
            @CurrentUser UserPrincipal currentUser) {
        
        log.debug("Getting project by key: {} for user: {}", projectKey, currentUser.getId());
        ProjectResponseDTO project = projectService.getProjectByKey(projectKey, currentUser.getId());
        return ResponseEntity.ok(project);
    }

    /**
     * Update project details.
     * Only OWNER and ADMIN can update.
     */
    @PutMapping("/{projectId}")
    @Operation(
        summary = "Update project",
        description = "Update project details. Only OWNER and ADMIN can update."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Project updated successfully",
            content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable @Parameter(description = "Project ID") UUID projectId,
            @Valid @RequestBody ProjectUpdateRequestDTO request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Updating project: {} by user: {}", projectId, currentUser.getId());
        ProjectResponseDTO project = projectService.updateProject(projectId, request, currentUser.getId());
        return ResponseEntity.ok(project);
    }

    /**
     * Archive project.
     * Only OWNER can archive.
     */
    @PostMapping("/{projectId}/archive")
    @Operation(
        summary = "Archive project",
        description = "Archive a project. Only OWNER can archive."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Project archived successfully"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> archiveProject(
            @PathVariable @Parameter(description = "Project ID") UUID projectId,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Archiving project: {} by user: {}", projectId, currentUser.getId());
        projectService.archiveProject(projectId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Restore archived project.
     * Only OWNER can restore.
     */
    @PostMapping("/{projectId}/restore")
    @Operation(
        summary = "Restore project",
        description = "Restore an archived project. Only OWNER can restore."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Project restored successfully"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> restoreProject(
            @PathVariable @Parameter(description = "Project ID") UUID projectId,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Restoring project: {} by user: {}", projectId, currentUser.getId());
        projectService.restoreProject(projectId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete project permanently.
     * Only OWNER can delete.
     */
    @DeleteMapping("/{projectId}")
    @Operation(
        summary = "Delete project",
        description = "Delete a project permanently. Only OWNER can delete."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteProject(
            @PathVariable @Parameter(description = "Project ID") UUID projectId,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Deleting project: {} by user: {}", projectId, currentUser.getId());
        projectService.deleteProject(projectId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get current user's projects.
     * Excludes archived projects unless user is OWNER.
     */
    @GetMapping("/my-projects")
    @Operation(
        summary = "Get my projects",
        description = "Get all projects the current user is a member of. Excludes archived projects unless user is OWNER."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Projects retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<ProjectResponseDTO>> getMyProjects(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        
        log.debug("Getting projects for user: {}", currentUser.getId());
        Page<ProjectResponseDTO> projects = projectService.getUserProjects(currentUser.getId(), pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Get archived projects where user is OWNER.
     */
    @GetMapping("/archived")
    @Operation(
        summary = "Get archived projects",
        description = "Get all archived projects where the current user is OWNER."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Archived projects retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<ProjectResponseDTO>> getArchivedProjects(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "archivedAt", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        
        log.debug("Getting archived projects for user: {}", currentUser.getId());
        Page<ProjectResponseDTO> projects = projectService.getArchivedProjects(currentUser.getId(), pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Search projects user can access.
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search projects",
        description = "Search projects by name or key that the user can access."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Projects found",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<ProjectResponseDTO>> searchProjects(
            @RequestParam @Parameter(description = "Search term (matches name or key)") String searchTerm,
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        
        log.debug("Searching projects with term: {} for user: {}", searchTerm, currentUser.getId());
        Page<ProjectResponseDTO> projects = projectService.searchProjects(searchTerm, currentUser.getId(), pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Get all public projects.
     * No authentication required.
     */
    @GetMapping("/list/public")
    @Operation(
        summary = "Get public projects",
        description = "Get all active public projects. No authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Public projects retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        )
    })
    public ResponseEntity<Page<ProjectResponseDTO>> getPublicProjects(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        
        log.debug("Getting public projects");
        Page<ProjectResponseDTO> projects = projectService.getPublicProjects(pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Check if project key exists.
     */
    @GetMapping("/exists/{projectKey}")
    @Operation(
        summary = "Check project key exists",
        description = "Check if a project key already exists."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Existence check completed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Boolean> checkProjectKeyExists(
            @PathVariable @Parameter(description = "Project key to check") String projectKey) {
        
        log.debug("Checking if project key exists: {}", projectKey);
        boolean exists = projectService.existsByKey(projectKey);
        return ResponseEntity.ok(exists);
    }
}