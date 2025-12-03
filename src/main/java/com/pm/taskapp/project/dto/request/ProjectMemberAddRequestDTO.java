package com.pm.taskapp.project.dto.request;

import com.pm.taskapp.project.enums.ProjectRole;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

/**
 * DTO for adding a member to a project.
 * 
 * <p>Validation rules:
 * <ul>
 *   <li>User ID: required, must be valid UUID</li>
 *   <li>Role: required, must be assignable (not OWNER)</li>
 * </ul>
 * 
 * <p>Note: OWNER role cannot be directly assigned.
 * Use ownership transfer endpoint instead.
 * 
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberAddRequestDTO {

    /**
     * ID of the user to add as member.
     */
    @NotNull(message = "User ID is required")
    private UUID userId;

    /**
     * Role to assign to the member.
     * Defaults to MEMBER if not specified.
     */
    @NotNull(message = "Role is required")
    @Builder.Default
    private ProjectRole role = ProjectRole.MEMBER;

    /**
     * Validates that role is assignable.
     * OWNER role cannot be directly assigned.
     */
    @AssertTrue(message = "OWNER role cannot be directly assigned. Use ownership transfer instead")
    public boolean isRoleAssignable() {
        if (role == null) {
            return true; // Will use default
        }
        return ProjectRole.isAssignable(role);
    }
}