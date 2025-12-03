package com.pm.taskapp.project.dto.request;

import com.pm.taskapp.project.enums.ProjectVisibility;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for updating an existing project.
 * All fields are optional - only provided fields will be updated.
 * 
 * <p>Validation rules:
 * <ul>
 *   <li>Name: if provided, 3-255 characters</li>
 *   <li>Description: if provided, max 5000 characters</li>
 *   <li>Visibility: if provided, must be valid enum value</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateRequestDTO {

    /**
     * Updated project name.
     * Optional - only updates if provided.
     */
    @Size(min = 3, max = 255, message = "Project name must be between 3 and 255 characters")
    private String name;

    /**
     * Updated project description.
     * Optional - only updates if provided.
     * Can be null to clear description.
     */
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    /**
     * Updated visibility level.
     * Optional - only updates if provided.
     */
    private ProjectVisibility visibility;

    /**
     * Validates visibility value if provided.
     */
    @AssertTrue(message = "Visibility must be either PUBLIC or PRIVATE")
    public boolean isValidVisibility() {
        if (visibility == null) {
            return true; // No update to visibility
        }
        return visibility == ProjectVisibility.PUBLIC || visibility == ProjectVisibility.PRIVATE;
    }

    /**
     * Checks if at least one field is provided for update.
     */
    @AssertTrue(message = "At least one field must be provided for update")
    public boolean hasAtLeastOneField() {
        return name != null || description != null || visibility != null;
    }
}