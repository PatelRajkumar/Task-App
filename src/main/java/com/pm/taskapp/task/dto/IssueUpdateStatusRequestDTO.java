package com.pm.taskapp.task.dto;

import com.pm.taskapp.task.enums.IssueStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating issue status.
 * Used for status transitions with workflow validation.
 *
 * <p>Validation rules:
 * <ul>
 *   <li>New status: required, must follow workflow (TODO → INPROGRESS → DONE)</li>
 *   <li>Reason: optional, max 500 characters</li>
 * </ul>
 *
 * <p>Note: Status transitions are validated against workflow rules in service layer.
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueUpdateStatusRequestDTO {

    @NotNull(message = "New status is required")
    private IssueStatus newStatus;

    // Optional: reason for status change (for audit trail)
    @Size(max = 500, message = "Status change reason must not exceed 500 characters")
    private String reason;
}
