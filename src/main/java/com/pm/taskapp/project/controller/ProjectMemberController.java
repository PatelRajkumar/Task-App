package com.pm.taskapp.project.controller;

import com.pm.taskapp.auth.security.CurrentUser;
import com.pm.taskapp.auth.security.UserPrincipal;
import com.pm.taskapp.project.dto.request.ProjectMemberAddRequestDTO;
import com.pm.taskapp.project.dto.request.ProjectMemberUpdateRoleRequestDTO;
import com.pm.taskapp.project.dto.request.TransferOwnershipRequestDTO;
import com.pm.taskapp.project.dto.response.ProjectMemberResponseDTO;
import com.pm.taskapp.project.dto.response.ProjectMemberSummaryDTO;
import com.pm.taskapp.project.service.ProjectMemberService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for project member management operations.
 * Provides endpoints for adding, removing, and updating project members.
 * 
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
@Tag(name = "Project Members", description = "Project member management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    /**
     * Add member to project.
     * Only OWNER and ADMIN can add members.
     */
    @PostMapping
    @Operation(
        summary = "Add project member",
        description = "Add a new member to the project. Only OWNER and ADMIN can add members. Cannot directly assign OWNER role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Member added successfully",
            content = @Content(schema = @Schema(implementation = ProjectMemberResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data or OWNER role assignment"),
        @ApiResponse(responseCode = "404", description = "Project not found or user not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "409", description = "User is already a member"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ProjectMemberResponseDTO> addMember(
            @PathVariable @Parameter(description = "Project ID") UUID projectId,
            @Valid @RequestBody ProjectMemberAddRequestDTO request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Adding member {} to project {} by user {}", 
                request.getUserId(), projectId, currentUser.getId());
        
        ProjectMemberResponseDTO member = projectMemberService.addMember(
                projectId, request, currentUser.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    /**
     * Get all project members.
     * Returns members with their permissions.
     */
    @GetMapping
    @Operation(
        summary = "Get project members",
        description = "Get all members of the project with their permissions. Requires project membership."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Members retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Not a project member"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<ProjectMemberSummaryDTO>> getProjectMembers(
            @PathVariable @Parameter(description = "Project ID") UUID projectId,
            @CurrentUser UserPrincipal currentUser) {
        
        log.debug("Getting members for project: {}", projectId);
        List<ProjectMemberSummaryDTO> members = projectMemberService.getProjectMembers(
                projectId, currentUser.getId());
        
        return ResponseEntity.ok(members);
    }

    /**
     * Get specific member details.
     */
    @GetMapping("/{userId}")
    @Operation(
        summary = "Get member details",
        description = "Get details of a specific project member. Requires project membership."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Member details retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProjectMemberResponseDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Project or member not found"),
        @ApiResponse(responseCode = "403", description = "Not a project member"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ProjectMemberResponseDTO> getMember(
            @PathVariable @Parameter(description = "Project ID") UUID projectId,
            @PathVariable @Parameter(description = "User ID") UUID userId,
            @CurrentUser UserPrincipal currentUser) {
        
        log.debug("Getting member {} from project: {}", userId, projectId);
        ProjectMemberResponseDTO member = projectMemberService.getMember(
                projectId, userId, currentUser.getId());
        
        return ResponseEntity.ok(member);
    }

    /**
     * Update member's role.
     * Only OWNER and ADMIN can update roles.
     */
    @PutMapping("/{userId}/role")
    @Operation(
        summary = "Update member role",
        description = "Update a member's role. Only OWNER and ADMIN can update. Cannot change to OWNER role (use transfer ownership). ADMIN can only update MEMBER and VIEWER roles."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role updated successfully",
            content = @Content(schema = @Schema(implementation = ProjectMemberResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid role or trying to assign OWNER role"),
        @ApiResponse(responseCode = "404", description = "Project or member not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ProjectMemberResponseDTO> updateMemberRole(
            @PathVariable @Parameter(description = "Project ID") UUID projectId,
            @PathVariable @Parameter(description = "User ID") UUID userId,
            @Valid @RequestBody ProjectMemberUpdateRoleRequestDTO request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Updating role of member {} in project {} to {} by user {}", 
                userId, projectId, request.getRole(), currentUser.getId());
        
        ProjectMemberResponseDTO member = projectMemberService.updateMemberRole(
                projectId, userId, request, currentUser.getId());
        
        return ResponseEntity.ok(member);
    }

    /**
     * Remove member from project.
     * Only OWNER and ADMIN can remove members.
     */
    @DeleteMapping("/{userId}")
    @Operation(
        summary = "Remove project member",
        description = "Remove a member from the project. Only OWNER and ADMIN can remove members. Cannot remove the last OWNER. ADMIN cannot remove OWNER or other ADMINs."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Member removed successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot remove last OWNER"),
        @ApiResponse(responseCode = "404", description = "Project or member not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> removeMember(
            @PathVariable @Parameter(description = "Project ID") UUID projectId,
            @PathVariable @Parameter(description = "User ID") UUID userId,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Removing member {} from project {} by user {}", 
                userId, projectId, currentUser.getId());
        
        projectMemberService.removeMember(projectId, userId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Transfer project ownership.
     * Only current OWNER can transfer.
     */
    @PostMapping("/transfer-ownership")
    @Operation(
        summary = "Transfer project ownership",
        description = "Transfer project ownership to another member. Only current OWNER can transfer. New owner must already be a project member. Current owner becomes ADMIN after transfer."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Ownership transferred successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or transferring to self"),
        @ApiResponse(responseCode = "404", description = "Project or new owner not found"),
        @ApiResponse(responseCode = "403", description = "Only OWNER can transfer ownership"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> transferOwnership(
            @PathVariable @Parameter(description = "Project ID") UUID projectId,
            @Valid @RequestBody TransferOwnershipRequestDTO request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Transferring ownership of project {} from {} to {}", 
                projectId, currentUser.getId(), request.getNewOwnerUserId());
        
        projectMemberService.transferOwnership(projectId, request, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Leave project.
     * User removes themselves from project.
     */
    @PostMapping("/leave")
    @Operation(
        summary = "Leave project",
        description = "Leave the project (remove yourself as a member). Cannot leave if you're the last OWNER - must transfer ownership first."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Left project successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot leave as last OWNER"),
        @ApiResponse(responseCode = "404", description = "Project not found or not a member"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> leaveProject(
            @PathVariable @Parameter(description = "Project ID") UUID projectId,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("User {} leaving project {}", currentUser.getId(), projectId);
        projectMemberService.leaveProject(projectId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}