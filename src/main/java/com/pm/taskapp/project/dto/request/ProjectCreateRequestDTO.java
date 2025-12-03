package com.pm.taskapp.project.dto.request;

import com.pm.taskapp.project.enums.ProjectVisibility;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating a new project.
 * Contains all required information for project creation.
 * 
 * <p>Validation rules:
 * <ul>
 *   <li>Name: required, 3-255 characters</li>
 *   <li>Description: optional, max 5000 characters</li>
 *   <li>Visibility: defaults to PRIVATE if not specified</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequestDTO {

    /**
     * Project name.
     * Must be unique and descriptive.
     */
    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 255, message = "Project name must be between 3 and 255 characters")
    private String name;

    /**
     * Project description.
     * Supports markdown formatting.
     */
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    /**
     * Project visibility level.
     * Defaults to PRIVATE if not specified.
     */
    @Builder.Default
    private ProjectVisibility visibility = ProjectVisibility.PRIVATE;

    /**
     * Validates visibility value if provided.
     */
    @AssertTrue(message = "Visibility must be either PUBLIC or PRIVATE")
    public boolean isValidVisibility() {
        if (visibility == null) {
            return true; // Will use default
        }
        return visibility == ProjectVisibility.PUBLIC || visibility == ProjectVisibility.PRIVATE;
    }
}