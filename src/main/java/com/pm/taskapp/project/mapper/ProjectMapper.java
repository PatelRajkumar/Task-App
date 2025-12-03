package com.pm.taskapp.project.mapper;

import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.project.dto.response.*;
import com.pm.taskapp.project.entity.Project;
import com.pm.taskapp.project.entity.ProjectIssueCounter;
import com.pm.taskapp.project.entity.ProjectMember;
import com.pm.taskapp.project.enums.ProjectRole;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper for converting Project entity to DTOs.
 * Follows the same pattern as UserMapper in auth module.
 * 
 * @since 1.0.0
 */
@Component
public class ProjectMapper {

    /**
     * Convert Project entity to ProjectResponseDTO.
     * 
     * @param project Project entity
     * @return ProjectResponseDTO
     */
    public ProjectResponseDTO toResponseDTO(Project project) {
        if (project == null) {
            return null;
        }

        ProjectResponseDTO.ProjectResponseDTOBuilder builder = ProjectResponseDTO.builder()
                .id(project.getId())
                .key(project.getKey())
                .name(project.getName())
                .description(project.getDescription())
                .visibility(project.getVisibility())
                .isArchived(project.getIsArchived())
                .archivedAt(project.getArchivedAt())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt());

        // Map creator
        if (project.getCreatedBy() != null) {
            builder.createdBy(toUserSummaryDTO(project.getCreatedBy()));
        }

        return builder.build();
    }

    /**
     * Convert Project entity to ProjectResponseDTO with counts.
     * 
     * @param project Project entity
     * @param memberCount Number of members
     * @param issueCount Number of issues
     * @return ProjectResponseDTO with counts
     */
    public ProjectResponseDTO toResponseDTOWithCounts(Project project, Integer memberCount, Integer issueCount) {
        if (project == null) {
            return null;
        }

        ProjectResponseDTO dto = toResponseDTO(project);
        dto.setMemberCount(memberCount);
        dto.setIssueCount(issueCount);

        return dto;
    }

    /**
     * Convert Project entity to ProjectResponseDTO with current user's role.
     * 
     * @param project Project entity
     * @param currentUserRole Current user's role in project (null if not a member)
     * @return ProjectResponseDTO with user role
     */
    public ProjectResponseDTO toResponseDTOWithUserRole(Project project, ProjectRole currentUserRole) {
        if (project == null) {
            return null;
        }

        ProjectResponseDTO dto = toResponseDTO(project);
        if (currentUserRole != null) {
            dto.setCurrentUserRole(currentUserRole.name());
        }

        return dto;
    }

    /**
     * Convert Project entity to ProjectSummaryDTO.
     * 
     * @param project Project entity
     * @return ProjectSummaryDTO
     */
    public ProjectSummaryDTO toSummaryDTO(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectSummaryDTO.builder()
                .id(project.getId())
                .key(project.getKey())
                .name(project.getName())
                .visibility(project.getVisibility())
                .isArchived(project.getIsArchived())
                .build();
    }

    /**
     * Convert User entity to UserSummaryDTO.
     * 
     * @param user User entity
     * @return UserSummaryDTO
     */
    public UserSummaryDTO toUserSummaryDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    /**
     * Get issue count from counter entity.
     * 
     * @param counter ProjectIssueCounter entity
     * @return Issue count
     */
    public Integer getIssueCount(ProjectIssueCounter counter) {
        return counter != null ? counter.getLastNumber() : 0;
    }
}