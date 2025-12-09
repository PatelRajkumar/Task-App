package com.pm.taskapp.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for assigning an issue to a user.
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Assignee ID: required, must be valid UUID</li>
 *   <li>Assignee must be a project member (validated in service layer)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueAssignRequestDTO {

    private UUID assigneeId;
}