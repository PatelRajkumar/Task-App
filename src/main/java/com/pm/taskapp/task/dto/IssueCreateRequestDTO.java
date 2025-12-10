package com.pm.taskapp.task.dto;

import com.pm.taskapp.task.enums.IssuePriority;
import com.pm.taskapp.task.enums.IssueStatus;
import com.pm.taskapp.task.enums.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating a new issue.
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Title: required, 3-500 characters</li>
 *   <li>Description: optional, max 5000 characters</li>
 *   <li>Type: defaults to TASK if not specified</li>
 *   <li>Priority: defaults to MEDIUM if not specified</li>
 *   <li>Assignee: optional, can be null (unassigned)</li>
 *   <li>Due date: optional, validated in service layer</li>
 * </ul>
 *
 * <p>Note: Status is always TODO on creation.
 *
 * @since 1.0.0
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCreateRequestDTO {

    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 500, message = "Issue title must be between 3 and 500 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Builder.Default
    private IssueType type = IssueType.TASK;

    private LocalDate dueDate;

    @Builder.Default
    private IssuePriority priority = IssuePriority.MEDIUM;

    private UUID assigneeId;

}