package com.pm.taskapp.task.dto;

import com.pm.taskapp.task.enums.IssuePriority;
import com.pm.taskapp.task.enums.IssueType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for updating an existing issue.
 * All fields are optional - only provided fields will be updated.
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Title: if provided, 3-500 characters</li>
 *   <li>Description: if provided, max 5000 characters</li>
 *   <li>Priority: if provided, must be valid enum value</li>
 *   <li>Assignee: if provided, must be valid user ID (can be null to unassign)</li>
 *   <li>Due date: if provided, will be validated in service layer</li>
 * </ul>
 *
 * <p>Note:
 * - Status updates should use separate endpoint (PATCH /api/issues/{key}/status)
 * - Type cannot be changed after creation
 * - At least one field must be provided for update
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueUpdateRequestDTO {

    @Size(min = 3, max = 500, message = "Issue title must be between 3 and 500 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private LocalDate dueDate;

    private IssuePriority priority;

    private UUID assigneeId;

    @AssertTrue(message = "At least one field must be provided for update")
    public boolean hasAtLeastOneField() {
        return title != null
                || description != null
                || priority != null
                || assigneeId != null
                || dueDate != null;
    }
}
