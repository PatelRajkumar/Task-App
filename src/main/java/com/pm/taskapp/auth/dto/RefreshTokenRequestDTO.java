package com.pm.taskapp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for refresh token request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    /**
     * Whether to rotate the refresh token.
     * If true, a new refresh token will be generated and the old one invalidated.
     */
    @Builder.Default
    private boolean rotateRefreshToken = false;

    /**
     * Device information for security tracking.
     */
    private String deviceInfo;

    /**
     * IP address for security tracking.
     */
    private String ipAddress;
}