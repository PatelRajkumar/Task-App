package com.pm.taskapp.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pm.taskapp.project.enums.ProjectVisibility;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for project response.
 * Contains all public project information.
 * 
 * <p>Usage:
 * <ul>
 *   <li>Project list responses</li>
 *   <li>Project detail responses</li>
 *   <li>Project creation responses</li>
 *   <li>Project update responses</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectResponseDTO {

    /**
     * Project unique identifier.
     */
    private UUID id;

    /**
     * Project unique key.
     * Format: PROJ-{number} (e.g., PROJ-1, PROJ-2)
     */
    private String key;

    /**
     * Project name.
     */
    private String name;

    /**
     * Project description.
     * May contain markdown formatting.
     */
    private String description;

    /**
     * Project visibility level.
     */
    private ProjectVisibility visibility;

    /**
     * User who created the project.
     * Simplified user info (id, name, email).
     */
    private UserSummaryDTO createdBy;

    /**
     * Is project archived.
     * Archived projects are only visible to OWNERs.
     */
    private Boolean isArchived;

    /**
     * When the project was archived.
     * Null if not archived.
     */
    private Instant archivedAt;

    /**
     * When the project was created.
     */
    private Instant createdAt;

    /**
     * When the project was last updated.
     */
    private Instant updatedAt;

    /**
     * Number of members in the project.
     * Optional - only included if requested.
     */
    private Integer memberCount;

    /**
     * Number of issues in the project.
     * Optional - only included if requested.
     */
    private Integer issueCount;

    /**
     * Current user's role in the project.
     * Optional - only included if user is a member.
     */
    private String currentUserRole;
}