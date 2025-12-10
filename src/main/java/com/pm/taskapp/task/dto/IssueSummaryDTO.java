package com.pm.taskapp.task.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pm.taskapp.project.dto.response.UserSummaryDTO;
import com.pm.taskapp.task.enums.IssuePriority;
import com.pm.taskapp.task.enums.IssueStatus;
import com.pm.taskapp.task.enums.IssueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for issue summary response.
 * Lightweight version for lists and search results.
 *
 * <p>Contains essential issue information without full details:
 * <ul>
 *   <li>Basic identification (id, key, title)</li>
 *   <li>Classification (type, priority, status)</li>
 *   <li>Assignment (assignee only, not reporter)</li>
 *   <li>Due date tracking</li>
 * </ul>
 *
 * <p>Use {@link IssueResponseDTO} for complete issue details.
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueSummaryDTO {
    private UUID id;
    private String key;
    private String title;
    private IssueType type;
    private IssuePriority priority;
    private IssueStatus status;
    private UserSummaryDTO assignee;  // Just assignee, not reporter
    private LocalDate dueDate;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Check if issue is overdue.
     */
    public boolean isOverdue() {
        return dueDate != null
                && dueDate.isBefore(LocalDate.now())
                && status != IssueStatus.DONE;
    }

    /**
     * Check if issue is assigned.
     */
    public boolean isAssigned() {
        return assignee != null;
    }
}
