package com.pm.taskapp.project.service;

import com.pm.taskapp.project.dto.request.ProjectMemberAddRequestDTO;
import com.pm.taskapp.project.dto.request.ProjectMemberUpdateRoleRequestDTO;
import com.pm.taskapp.project.dto.request.TransferOwnershipRequestDTO;
import com.pm.taskapp.project.dto.response.ProjectMemberResponseDTO;
import com.pm.taskapp.project.dto.response.ProjectMemberSummaryDTO;
import com.pm.taskapp.project.enums.ProjectRole;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for project member management operations.
 * Handles adding, removing, and updating project members.
 * 
 * @since 1.0.0
 */
public interface ProjectMemberService {

    /**
     * Add member to project.
     * Only OWNER and ADMIN can add members.
     * Cannot directly add OWNER role.
     * 
     * @param projectId Project ID
     * @param request Member addition data
     * @param currentUserId Current user ID for permission check
     * @return Added member response
     */
    ProjectMemberResponseDTO addMember(UUID projectId, ProjectMemberAddRequestDTO request, UUID currentUserId);

    /**
     * Remove member from project.
     * Only OWNER and ADMIN can remove members.
     * Cannot remove the last OWNER.
     * ADMIN cannot remove OWNER or other ADMINs.
     * 
     * @param projectId Project ID
     * @param userId User ID to remove
     * @param currentUserId Current user ID for permission check
     */
    void removeMember(UUID projectId, UUID userId, UUID currentUserId);

    /**
     * Update member's role.
     * Only OWNER and ADMIN can update roles.
     * Cannot change role to OWNER (use transfer ownership).
     * Cannot change the last OWNER's role.
     * ADMIN can only update MEMBER and VIEWER roles.
     * 
     * @param projectId Project ID
     * @param userId User ID whose role to update
     * @param request Role update data
     * @param currentUserId Current user ID for permission check
     * @return Updated member response
     */
    ProjectMemberResponseDTO updateMemberRole(
            UUID projectId, 
            UUID userId, 
            ProjectMemberUpdateRoleRequestDTO request, 
            UUID currentUserId
    );

    /**
     * Transfer project ownership.
     * Only current OWNER can transfer.
     * New owner must already be a project member.
     * Current owner becomes ADMIN after transfer.
     * 
     * @param projectId Project ID
     * @param request Transfer ownership data
     * @param currentUserId Current user ID (must be OWNER)
     */
    void transferOwnership(UUID projectId, TransferOwnershipRequestDTO request, UUID currentUserId);

    /**
     * Get all members of a project.
     * Returns members with their permissions.
     * 
     * @param projectId Project ID
     * @param currentUserId Current user ID for access check
     * @return List of project members
     */
    List<ProjectMemberSummaryDTO> getProjectMembers(UUID projectId, UUID currentUserId);

    /**
     * Get member details.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @param currentUserId Current user ID for access check
     * @return Member response with permissions
     */
    ProjectMemberResponseDTO getMember(UUID projectId, UUID userId, UUID currentUserId);

    /**
     * Get current user's role in project.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return User's role or null if not a member
     */
    ProjectRole getUserRole(UUID projectId, UUID userId);

    /**
     * Check if user is a member of project.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return true if user is a member
     */
    boolean isMember(UUID projectId, UUID userId);

    /**
     * Check if user is OWNER of project.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return true if user is owner
     */
    boolean isOwner(UUID projectId, UUID userId);

    /**
     * Check if user is OWNER or ADMIN of project.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return true if user is owner or admin
     */
    boolean isOwnerOrAdmin(UUID projectId, UUID userId);

    /**
     * Count members in project.
     * 
     * @param projectId Project ID
     * @return Number of members
     */
    long countMembers(UUID projectId);

    /**
     * Count owners in project.
     * 
     * @param projectId Project ID
     * @return Number of owners
     */
    long countOwners(UUID projectId);

    /**
     * Leave project.
     * User removes themselves from project.
     * Cannot leave if you're the last OWNER.
     * 
     * @param projectId Project ID
     * @param userId User ID leaving
     */
    void leaveProject(UUID projectId, UUID userId);
}