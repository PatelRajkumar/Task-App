package com.pm.taskapp.project.dto.response;


import com.pm.taskapp.project.enums.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Simplified DTO for project member without redundant project details.
 * Used in member list endpoints where project context is already known.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberSummaryDTO {
    
    /**
     * Unique identifier of the project member record
     */
    private UUID id;
    
    /**
     * User details (id, name, email, avatar)
     */
    private UserSummaryDTO user;
    
    /**
     * Member's role in the project
     */
    private ProjectRole role;
    
    /**
     * Human-readable role name
     */
    private String roleDisplayName;
    
    /**
     * When the user joined the project
     */
    private Instant joinedAt;
    
    /**
     * Permissions based on the member's role
     */
    private MemberPermissionsDTO permissions;
}