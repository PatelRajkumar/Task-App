package com.pm.taskapp.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pm.taskapp.project.enums.ProjectVisibility;
import lombok.*;

import java.util.UUID;

/**
 * DTO for simplified project information.
 * Used in responses where full project details are not needed.
 * 
 * <p>Contains only essential project information:
 * <ul>
 *   <li>ID for identification</li>
 *   <li>Key for display</li>
 *   <li>Name for display</li>
 *   <li>Visibility for access control</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectSummaryDTO {

    /**
     * Project unique identifier.
     */
    private UUID id;

    /**
     * Project unique key.
     * Format: PROJ-{number}
     */
    private String key;

    /**
     * Project name.
     */
    private String name;

    /**
     * Project visibility level.
     */
    private ProjectVisibility visibility;

    /**
     * Is project archived.
     */
    private Boolean isArchived;
}