package com.pm.taskapp.task.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pm.taskapp.project.dto.response.UserSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueHistoryResponseDTO {
    private UUID id;
    private String field;            // "status", "assignee", "priority", "title"
    private String oldValue;
    private String newValue;
    private UserSummaryDTO changedBy;
    private Instant changedAt;

    public String getChangeDescription() {
        if (oldValue == null) {
            return String.format("Set %s to %s", field, newValue);
        } else if (newValue == null) {
            return String.format("Removed %s (was %s)", field, oldValue);
        } else {
            return String.format("Changed %s from %s to %s", field, oldValue, newValue);
        }
    }
}
