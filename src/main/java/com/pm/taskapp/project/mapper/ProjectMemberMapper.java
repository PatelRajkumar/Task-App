package com.pm.taskapp.project.mapper;

import com.pm.taskapp.auth.mapper.UserMapper;
import com.pm.taskapp.project.dto.response.MemberPermissionsDTO;
import com.pm.taskapp.project.dto.response.ProjectMemberResponseDTO;
import com.pm.taskapp.project.dto.response.ProjectMemberSummaryDTO;
import com.pm.taskapp.project.dto.response.ProjectSummaryDTO;
import com.pm.taskapp.project.dto.response.UserSummaryDTO;
import com.pm.taskapp.project.entity.ProjectMember;
import com.pm.taskapp.project.enums.ProjectRole;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Mapper for converting ProjectMember entity to DTOs.
 * 
 * @since 1.0.0
 */
@Component
public class ProjectMemberMapper {

    private final ProjectMapper projectMapper;

    public ProjectMemberMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    /**
     * Convert ProjectMember entity to ProjectMemberResponseDTO.
     * 
     * @param member ProjectMember entity
     * @return ProjectMemberResponseDTO
     */
    public ProjectMemberResponseDTO toResponseDTO(ProjectMember member) {
        if (member == null) {
            return null;
        }

        ProjectMemberResponseDTO.ProjectMemberResponseDTOBuilder builder = ProjectMemberResponseDTO.builder()
                .id(member.getId())
                .role(member.getRole())
                .roleDisplayName(member.getRole() != null ? member.getRole().getDisplayName() : null)
                .joinedAt(member.getJoinedAt());

        // Map project summary
        if (member.getProject() != null) {
            builder.project(projectMapper.toSummaryDTO(member.getProject()));
        }

        // Map user summary
        if (member.getUser() != null) {
            builder.user(projectMapper.toUserSummaryDTO(member.getUser()));
        }

        return builder.build();
    }

    /**
     * Convert ProjectMember entity to ProjectMemberResponseDTO with permissions.
     * 
     * @param member ProjectMember entity
     * @return ProjectMemberResponseDTO with permissions
     */
    public ProjectMemberResponseDTO toResponseDTOWithPermissions(ProjectMember member) {
        if (member == null) {
            return null;
        }

        ProjectMemberResponseDTO dto = toResponseDTO(member);
        dto.setPermissions(toPermissionsDTO(member.getRole()));

        return dto;
    }

    /**
     * Convert ProjectRole to MemberPermissionsDTO.
     * 
     * @param role ProjectRole
     * @return MemberPermissionsDTO
     */
    public MemberPermissionsDTO toPermissionsDTO(ProjectRole role) {
        if (role == null) {
            return MemberPermissionsDTO.builder()
                    .canDelete(false)
                    .canManageMembers(false)
                    .canEditProject(false)
                    .canCreateIssues(false)
                    .canViewProject(false)
                    .canArchiveProject(false)
                    .canTransferOwnership(false)
                    .build();
        }

        return MemberPermissionsDTO.builder()
                .canDelete(role.canDelete())
                .canManageMembers(role.canManageMembers())
                .canEditProject(role.canEditProject())
                .canCreateIssues(role.canCreateIssues())
                .canViewProject(role.canViewProject())
                .canArchiveProject(role == ProjectRole.OWNER)
                .canTransferOwnership(role == ProjectRole.OWNER)
                .build();
    }

    public ProjectMemberSummaryDTO toSummaryDTO(ProjectMember member) {
    if (member == null) {
        return null;
    }
    
    return ProjectMemberSummaryDTO.builder()
            .id(member.getId())
            .user(projectMapper.toUserSummaryDTO(member.getUser()))
            .role(member.getRole())
            .roleDisplayName(member.getRole().getDisplayName())
            .joinedAt(member.getJoinedAt())
            .permissions(toPermissionsDTO(member.getRole()))
            .build();
}

public List<ProjectMemberSummaryDTO> toSummaryDTOList(List<ProjectMember> members) {
    if (members == null) {
        return null;
    }
    
    return members.stream()
            .map(this::toSummaryDTO)
            .collect(Collectors.toList());
}
}