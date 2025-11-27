package com.pm.taskapp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for login request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    /**
     * Remember me flag for extended session.
     */
    @Builder.Default
    private boolean rememberMe = false;

    /**
     * Device information for security tracking.
     */
    private String deviceInfo;

    /**
     * IP address for security tracking.
     */
    private String ipAddress;

    /**
     * User agent for security tracking.
     */
    private String userAgent;
}