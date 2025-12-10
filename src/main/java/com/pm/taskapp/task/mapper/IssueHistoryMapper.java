package com.pm.taskapp.task.mapper;

import com.pm.taskapp.project.mapper.ProjectMapper;
import com.pm.taskapp.task.dto.IssueHistoryResponseDTO;
import com.pm.taskapp.task.entity.IssueHistory;
import org.springframework.stereotype.Component;

@Component
public class IssueHistoryMapper {

    private final ProjectMapper projectMapper;

    public IssueHistoryMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public IssueHistoryResponseDTO toResponseDTO(IssueHistory issueHistory) {
        if(issueHistory == null) {
            return null;
        }
        IssueHistoryResponseDTO.IssueHistoryResponseDTOBuilder builder = IssueHistoryResponseDTO.builder()
                .id(issueHistory.getId())
                .field(issueHistory.getField())
                .oldValue(issueHistory.getOldValue())
                .newValue(issueHistory.getNewValue())
                .changedBy(projectMapper.toUserSummaryDTO(issueHistory.getChangedBy()))
                .changedAt(issueHistory.getChangedAt());

        return  builder.build();
    }
}
