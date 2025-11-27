package com.pm.taskapp.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for updating permission information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdateDTO {

    @Size(min = 3, max = 100, message = "Permission code must be between 3 and 100 characters")
    @Pattern(
            regexp = "^[A-Z][A-Z_]*$",
            message = "Permission code must contain only uppercase letters and underscores, and start with a letter"
    )
    private String code;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;
}