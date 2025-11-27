package com.pm.taskapp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for creating a new permission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCreateDTO {

    @NotBlank(message = "Permission code is required")
    @Size(min = 3, max = 100, message = "Permission code must be between 3 and 100 characters")
    @Pattern(
            regexp = "^[A-Z][A-Z_]*$",
            message = "Permission code must contain only uppercase letters and underscores, and start with a letter"
    )
    private String code;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Category or module this permission belongs to.
     */
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    /**
     * Whether this is a system permission that cannot be deleted.
     */
    @Builder.Default
    private boolean systemPermission = false;
}