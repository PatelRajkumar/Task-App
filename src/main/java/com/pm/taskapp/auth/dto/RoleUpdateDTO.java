package com.pm.taskapp.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for updating role information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateDTO {

    @Size(min = 3, max = 50, message = "Role name must be between 3 and 50 characters")
    @Pattern(
            regexp = "^ROLE_[A-Z_]+$",
            message = "Role name must start with 'ROLE_' and contain only uppercase letters and underscores"
    )
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Priority for role hierarchy.
     */
    private Integer priority;
}