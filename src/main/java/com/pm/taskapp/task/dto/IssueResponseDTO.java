package com.pm.taskapp.task.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pm.taskapp.project.dto.response.ProjectSummaryDTO;
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
 * DTO for issue response.
 * Contains complete issue information with related entity summaries.
 *
 * <p>Includes:
 * <ul>
 *   <li>Basic issue data (title, description, type, priority, status)</li>
 *   <li>Project summary (key, name)</li>
 *   <li>Reporter and assignee summaries (name, email, avatar)</li>
 *   <li>Timeline information (created, updated, resolved, closed)</li>
 *   <li>Due date tracking</li>
 * </ul>
 *
 * <p>Note: Soft-deleted issues are filtered out at repository level
 * and do not appear in normal API responses.
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueResponseDTO {

    private UUID id;

    private String key;

    private String title;

    private String description;

    private ProjectSummaryDTO project;

    private Integer sequentialNumber;

    private IssueType type;

    private LocalDate dueDate;

    private IssuePriority priority;

    private IssueStatus status;

    private UserSummaryDTO reporter;

    private UserSummaryDTO assignee;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant resolvedAt;

    private Instant closedAt;

    public boolean isOverdue() {
        return dueDate != null
                && dueDate.isBefore(LocalDate.now())
                && status != IssueStatus.DONE;
    }

    public boolean isAssigned() {
        return assignee != null;
    }

}
