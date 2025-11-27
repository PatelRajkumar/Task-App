package com.pm.taskapp.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for user registration request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

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

    @NotBlank(message = "Password confirmation is required")
    private String passwordConfirm;

    @Size(max = 1000, message = "Avatar URL must not exceed 1000 characters")
    private String avatarUrl;

    /**
     * Terms and conditions acceptance.
     */
    @NotNull(message = "You must accept the terms and conditions")
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean acceptTerms;

    /**
     * Marketing emails opt-in.
     */
    @Builder.Default
    private boolean subscribeToNewsletter = false;

    /**
     * Referral code if applicable.
     */
    private String referralCode;

    /**
     * Validate that passwords match.
     */
    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordsMatch() {
        return password != null && password.equals(passwordConfirm);
    }
}