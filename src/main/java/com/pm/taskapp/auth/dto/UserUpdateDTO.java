package com.pm.taskapp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for updating user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Avatar URL must not exceed 1000 characters")
    @Pattern(
            regexp = "^(https?://.*)?$",
            message = "Avatar URL must be a valid URL"
    )
    private String avatarUrl;

    /**
     * Whether the user should be enabled/disabled.
     * This field is typically only used by admin users.
     */
    private Boolean enabled;
}