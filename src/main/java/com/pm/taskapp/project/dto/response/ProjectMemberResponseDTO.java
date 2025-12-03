package com.pm.taskapp.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pm.taskapp.project.enums.ProjectRole;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for project member response.
 * Contains member information with their role and metadata.
 * 
 * <p>Usage:
 * <ul>
 *   <li>Project member list responses</li>
 *   <li>Member addition responses</li>
 *   <li>Member role update responses</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectMemberResponseDTO {

    /**
     * Member record unique identifier.
     */
    private UUID id;

    /**
     * Project information.
     * Simplified project summary.
     */
    private ProjectSummaryDTO project;

    /**
     * User information.
     * Simplified user summary.
     */
    private UserSummaryDTO user;

    /**
     * Member's role in the project.
     */
    private ProjectRole role;

    /**
     * Role display name.
     * Human-readable role name.
     */
    private String roleDisplayName;

    /**
     * When the user joined the project.
     */
    private Instant joinedAt;

    /**
     * Member permissions.
     * Optional - only included if requested.
     */
    private MemberPermissionsDTO permissions;
}