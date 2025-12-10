package com.pm.taskapp.task.mapper;

import com.pm.taskapp.auth.mapper.UserMapper;
import com.pm.taskapp.project.mapper.ProjectMapper;
import com.pm.taskapp.task.dto.IssueResponseDTO;
import com.pm.taskapp.task.dto.IssueSummaryDTO;
import com.pm.taskapp.task.entity.Issue;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Issue entity to DTOs.
 * Handles mapping to both full response and summary DTOs.
 *
 * @since 1.0.0
 */
@Component
public class IssueMapper {

    private final ProjectMapper projectMapper;

    public IssueMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public IssueResponseDTO toResponseDTO(Issue issue) {
        if(issue == null){
            return null;
        }
        IssueResponseDTO.IssueResponseDTOBuilder builder = IssueResponseDTO.builder()
                .id(issue.getId())
                .key(issue.getKey())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .project(projectMapper.toSummaryDTO(issue.getProject()))
                .sequentialNumber(issue.getSequentialNumber())
                .type(issue.getType())
                .dueDate(issue.getDueDate())
                .priority(issue.getPriority())
                .status(issue.getStatus())
                .reporter(projectMapper.toUserSummaryDTO(issue.getReporter()))
                .assignee(projectMapper.toUserSummaryDTO(issue.getAssignee()))
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .resolvedAt(issue.getResolvedAt())
                .closedAt(issue.getClosedAt());

        return builder.build();
    }

    public IssueSummaryDTO toSummaryDTO(Issue issue){
        if(issue == null){
            return null;
        }

        return IssueSummaryDTO.builder()
                .id(issue.getId())
                .key(issue.getKey())
                .title(issue.getTitle())
                .type(issue.getType())
                .priority(issue.getPriority())
                .status(issue.getStatus())
                .assignee(projectMapper.toUserSummaryDTO(issue.getAssignee()))
                .dueDate(issue.getDueDate())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }
}
