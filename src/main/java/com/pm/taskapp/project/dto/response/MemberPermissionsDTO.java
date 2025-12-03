package com.pm.taskapp.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * DTO for member permissions in a project.
 * Indicates what actions a member can perform.
 * 
 * <p>Permissions are derived from the member's role:
 * <ul>
 *   <li>OWNER: All permissions</li>
 *   <li>ADMIN: Manage members, edit project, create issues</li>
 *   <li>MEMBER: Create and edit issues</li>
 *   <li>VIEWER: View only</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberPermissionsDTO {

    /**
     * Can delete or permanently remove the project.
     */
    private boolean canDelete;

    /**
     * Can add, remove, or update members.
     */
    private boolean canManageMembers;

    /**
     * Can edit project settings (name, description, visibility).
     */
    private boolean canEditProject;

    /**
     * Can create new issues in the project.
     */
    private boolean canCreateIssues;

    /**
     * Can view the project.
     */
    private boolean canViewProject;

    /**
     * Can archive/restore the project.
     */
    private boolean canArchiveProject;

    /**
     * Can transfer ownership to another member.
     */
    private boolean canTransferOwnership;
}