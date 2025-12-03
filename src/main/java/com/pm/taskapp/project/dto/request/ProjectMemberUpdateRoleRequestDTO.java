package com.pm.taskapp.project.dto.request;

import com.pm.taskapp.project.enums.ProjectRole;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for updating a project member's role.
 * 
 * <p>Validation rules:
 * <ul>
 *   <li>Role: required, must be assignable (not OWNER)</li>
 * </ul>
 * 
 * <p>Business rules:
 * <ul>
 *   <li>OWNER role cannot be directly assigned</li>
 *   <li>Cannot change last OWNER to different role</li>
 *   <li>Must have permission to manage target role</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberUpdateRoleRequestDTO {

    /**
     * New role to assign.
     */
    @NotNull(message = "Role is required")
    private ProjectRole role;

    /**
     * Validates that role is assignable.
     * OWNER role cannot be directly assigned.
     */
    @AssertTrue(message = "OWNER role cannot be directly assigned. Use ownership transfer instead")
    public boolean isRoleAssignable() {
        if (role == null) {
            return false;
        }
        return ProjectRole.isAssignable(role);
    }
}