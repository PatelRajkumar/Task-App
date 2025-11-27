package com.pm.taskapp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

/**
 * DTO for creating a new role.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreateDTO {

    @NotBlank(message = "Role name is required")
    @Size(min = 3, max = 50, message = "Role name must be between 3 and 50 characters")
    @Pattern(
            regexp = "^ROLE_[A-Z_]+$",
            message = "Role name must start with 'ROLE_' and contain only uppercase letters and underscores"
    )
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Permission codes to assign to this role.
     */
    private Set<String> permissionCodes;

    /**
     * Whether this is a system role that cannot be deleted.
     */
    @Builder.Default
    private boolean systemRole = false;

    /**
     * Priority for role hierarchy (higher number = higher priority).
     */
    @Builder.Default
    private int priority = 0;
}