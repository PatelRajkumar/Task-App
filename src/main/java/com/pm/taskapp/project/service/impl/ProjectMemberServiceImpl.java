package com.pm.taskapp.project.service.impl;

import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.auth.repository.UserRepository;
import com.pm.taskapp.project.dto.request.ProjectMemberAddRequestDTO;
import com.pm.taskapp.project.dto.request.ProjectMemberUpdateRoleRequestDTO;
import com.pm.taskapp.project.dto.request.TransferOwnershipRequestDTO;
import com.pm.taskapp.project.dto.response.ProjectMemberResponseDTO;
import com.pm.taskapp.project.dto.response.ProjectMemberSummaryDTO;
import com.pm.taskapp.project.entity.Project;
import com.pm.taskapp.project.entity.ProjectMember;
import com.pm.taskapp.project.enums.ProjectRole;
import com.pm.taskapp.project.exception.*;
import com.pm.taskapp.project.mapper.ProjectMemberMapper;
import com.pm.taskapp.project.repository.ProjectMemberRepository;
import com.pm.taskapp.project.repository.ProjectRepository;
import com.pm.taskapp.project.service.ProjectMemberService;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ProjectMemberService for member management operations.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberMapper projectMemberMapper;
    private final EntityManager entityManager;

    @Override
    public ProjectMemberResponseDTO addMember(
            UUID projectId,
            ProjectMemberAddRequestDTO request,
            UUID currentUserId) {

        log.info("Adding member {} to project {} by user {}",
                request.getUserId(), projectId, currentUserId);

        // Verify project exists
        Project project = findProjectOrThrow(projectId);

        // Check permission (OWNER or ADMIN can add members)
        if (!isOwnerOrAdmin(projectId, currentUserId)) {
            throw ProjectAccessDeniedException.insufficientRole();
        }

        // Verify user exists
        User userToAdd = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with ID: " + request.getUserId()));

        // Check if already a member
        if (projectMemberRepository.existsByProject_IdAndUser_Id(projectId, request.getUserId())) {
            throw ProjectMemberAlreadyExistsException.forUser(request.getUserId());
        }

        // Validate role
        if (request.getRole() == ProjectRole.OWNER) {
            throw InvalidRoleAssignmentException.ownerRoleNotAssignable();
        }

        // Create member
        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(userToAdd)
                .role(request.getRole() != null ? request.getRole() : ProjectRole.MEMBER)
                .build();

        member = projectMemberRepository.save(member);
        log.info("Member added successfully: {} to project {}",
                userToAdd.getEmail(), project.getKey());

        return projectMemberMapper.toResponseDTOWithPermissions(member);
    }

    @Override
    public void removeMember(UUID projectId, UUID userId, UUID currentUserId) {
        log.info("Removing member {} from project {} by user {}",
                userId, projectId, currentUserId);

        // Verify project exists
        findProjectOrThrow(projectId);

        // Find member to remove
        ProjectMember memberToRemove = findMemberOrThrow(projectId, userId);

        // Get current user's member record
        ProjectMember currentMember = findMemberOrThrow(projectId, currentUserId);

        // Validate permissions
        validateCanManageMember(currentMember, memberToRemove);

        // Check if removing last owner
        if (memberToRemove.isOwner()) {
            long ownerCount = projectMemberRepository.countByProject_IdAndRole(
                    projectId, ProjectRole.OWNER);
            if (ownerCount <= 1) {
                throw LastOwnerRemovalException.cannotRemove();
            }
        }

        // Remove member
        projectMemberRepository.delete(memberToRemove);
        log.info("Member removed successfully from project: {}", projectId);
    }

    @Override
    public ProjectMemberResponseDTO updateMemberRole(
            UUID projectId,
            UUID userId,
            ProjectMemberUpdateRoleRequestDTO request,
            UUID currentUserId) {

        log.info("Updating role of member {} in project {} to {} by user {}",
                userId, projectId, request.getRole(), currentUserId);

        // Verify project exists
        findProjectOrThrow(projectId);

        // Find member to update
        ProjectMember memberToUpdate = findMemberOrThrow(projectId, userId);

        // Get current user's member record
        ProjectMember currentMember = findMemberOrThrow(projectId, currentUserId);

        // Validate permissions
        validateCanManageMember(currentMember, memberToUpdate);

        // Validate new role
        if (request.getRole() == ProjectRole.OWNER) {
            throw InvalidRoleAssignmentException.ownerRoleNotAssignable();
        }

        // Check if current role can manage target role
        if (!currentMember.getRole().canManageRole(request.getRole())) {
            throw InvalidRoleAssignmentException.cannotManageRole(request.getRole());
        }

        // Check if changing last owner's role
        if (memberToUpdate.isOwner()) {
            long ownerCount = projectMemberRepository.countByProject_IdAndRole(
                    projectId, ProjectRole.OWNER);
            if (ownerCount <= 1) {
                throw LastOwnerRemovalException.cannotChangeRole();
            }
        }

        // Update role
        memberToUpdate.updateRole(request.getRole());
        memberToUpdate = projectMemberRepository.save(memberToUpdate);

        log.info("Member role updated successfully in project: {}", projectId);

        return projectMemberMapper.toResponseDTOWithPermissions(memberToUpdate);
    }

    @Override
    public void transferOwnership(
            UUID projectId,
            TransferOwnershipRequestDTO request,
            UUID currentUserId) {

        log.info("Transferring ownership of project {} from {} to {}",
                projectId, currentUserId, request.getNewOwnerUserId());

        // Verify project exists
        findProjectOrThrow(projectId);

        // Verify current user is OWNER
        ProjectMember currentOwner = findMemberOrThrow(projectId, currentUserId);
        if (!currentOwner.isOwner()) {
            throw ProjectAccessDeniedException.forAction("transfer ownership");
        }

        // Verify new owner is already a member
        ProjectMember newOwner = findMemberOrThrow(projectId, request.getNewOwnerUserId());

        // Cannot transfer to self
        if (currentUserId.equals(request.getNewOwnerUserId())) {
            throw new IllegalArgumentException("Cannot transfer ownership to yourself");
        }

        // This ensures we always have at least one OWNER before demoting the current
        // one
        log.info("Promoting user {} to OWNER", request.getNewOwnerUserId());
        newOwner.setRole(ProjectRole.OWNER);
        projectMemberRepository.save(newOwner);

        entityManager.flush();

        log.info("Demoting user {} to ADMIN", currentUserId);
        currentOwner.setRole(ProjectRole.ADMIN);
        projectMemberRepository.save(currentOwner);
        

        log.info("Ownership transferred successfully for project: {}", projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberSummaryDTO> getProjectMembers(UUID projectId, UUID currentUserId) {
        log.debug("Getting members for project: {}", projectId);

        // Verify project exists
        findProjectOrThrow(projectId);

        // Check access
        if (!isMember(projectId, currentUserId)) {
            throw ProjectAccessDeniedException.notMember();
        }

        // Get members
        List<ProjectMember> members = projectMemberRepository.findByProject_IdOrderedByRole(projectId);

        return projectMemberMapper.toSummaryDTOList(members);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectMemberResponseDTO getMember(UUID projectId, UUID userId, UUID currentUserId) {
        log.debug("Getting member {} from project: {}", userId, projectId);

        // Verify project exists
        findProjectOrThrow(projectId);

        // Check access
        if (!isMember(projectId, currentUserId)) {
            throw ProjectAccessDeniedException.notMember();
        }

        // Find member
        ProjectMember member = findMemberOrThrow(projectId, userId);

        return projectMemberMapper.toResponseDTOWithPermissions(member);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectRole getUserRole(UUID projectId, UUID userId) {
        return projectMemberRepository.findUserRoleInProject(projectId, userId)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(UUID projectId, UUID userId) {
        return projectMemberRepository.existsByProject_IdAndUser_Id(projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(UUID projectId, UUID userId) {
        return projectMemberRepository.isOwner(projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwnerOrAdmin(UUID projectId, UUID userId) {
        return projectMemberRepository.isOwnerOrAdmin(projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMembers(UUID projectId) {
        return projectMemberRepository.countByProject_Id(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOwners(UUID projectId) {
        return projectMemberRepository.countByProject_IdAndRole(projectId, ProjectRole.OWNER);
    }

    @Override
    public void leaveProject(UUID projectId, UUID userId) {
        log.info("User {} leaving project {}", userId, projectId);

        // Verify project exists
        findProjectOrThrow(projectId);

        // Find member
        ProjectMember member = findMemberOrThrow(projectId, userId);

        // Check if leaving as last owner
        if (member.isOwner()) {
            long ownerCount = projectMemberRepository.countByProject_IdAndRole(
                    projectId, ProjectRole.OWNER);
            if (ownerCount <= 1) {
                throw LastOwnerRemovalException.cannotRemove();
            }
        }

        // Remove membership
        projectMemberRepository.delete(member);
        log.info("User left project successfully: {}", projectId);
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
     * Find project member or throw exception.
     */
    private ProjectMember findMemberOrThrow(UUID projectId, UUID userId) {
        return projectMemberRepository.findByProject_IdAndUser_Id(projectId, userId)
                .orElseThrow(() -> ProjectMemberNotFoundException.byIds(projectId, userId));
    }

    /**
     * Validate if current member can manage target member.
     */
    private void validateCanManageMember(ProjectMember currentMember, ProjectMember targetMember) {
        // Check if current user has permission to manage members
        if (!currentMember.canManageMembers()) {
            throw ProjectAccessDeniedException.insufficientRole();
        }

        // Check if current user can manage target role
        if (!currentMember.canManage(targetMember)) {
            throw InvalidRoleAssignmentException.cannotManageRole(targetMember.getRole());
        }
    }
}