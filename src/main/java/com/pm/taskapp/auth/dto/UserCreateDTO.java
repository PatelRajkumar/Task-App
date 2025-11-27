package com.pm.taskapp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

/**
 * DTO for creating a new user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, one special character, and no whitespace"
    )
    private String password;

    @Size(max = 1000, message = "Avatar URL must not exceed 1000 characters")
    @Pattern(
            regexp = "^(https?://.*)?$",
            message = "Avatar URL must be a valid URL"
    )
    private String avatarUrl;

    /**
     * Optional role names to assign to the user.
     * If not provided, default role will be assigned.
     */
    private Set<String> roleNames;

    /**
     * Whether to send welcome email.
     */
    @Builder.Default
    private boolean sendWelcomeEmail = true;

    /**
     * Whether to require email verification.
     */
    @Builder.Default
    private boolean requireEmailVerification = true;
}