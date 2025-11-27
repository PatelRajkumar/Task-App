package com.pm.taskapp.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * DTO for login response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {

    /**
     * JWT access token.
     */
    private String accessToken;

    /**
     * Refresh token for obtaining new access tokens.
     */
    private String refreshToken;

    /**
     * Token type (typically "Bearer").
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token expiration time in seconds.
     */
    private long expiresIn;

    /**
     * Authenticated user information.
     */
    private UserResponseDTO user;

    /**
     * Additional metadata about the session.
     */
    private SessionMetadataDTO sessionMetadata;

    /**
     * Whether two-factor authentication is required.
     */
    @Builder.Default
    private boolean requiresTwoFactor = false;

    /**
     * Two-factor authentication session token (if 2FA is required).
     */
    private String twoFactorSessionToken;
}