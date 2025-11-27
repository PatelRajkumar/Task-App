package com.pm.taskapp.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

/**
 * DTO for user metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMetadataDTO {

    /**
     * Number of failed login attempts.
     */
    private Integer failedLoginAttempts;

    /**
     * Last failed login attempt time.
     */
    private Instant lastFailedLoginAt;

    /**
     * Account locked until time.
     */
    private Instant lockedUntil;

    /**
     * Email verification status.
     */
    private boolean emailVerified;

    /**
     * Email verified at time.
     */
    private Instant emailVerifiedAt;

    /**
     * Two-factor authentication enabled.
     */
    private boolean twoFactorEnabled;

    /**
     * Phone number for 2FA.
     */
    private String phoneNumber;

    /**
     * Phone verified status.
     */
    private boolean phoneVerified;

    /**
     * Last password changed time.
     */
    private Instant passwordChangedAt;

    /**
     * Account creation source.
     */
    private String registrationSource;

    /**
     * Preferred language.
     */
    private String preferredLanguage;

    /**
     * Timezone.
     */
    private String timezone;

    /**
     * Total login count.
     */
    private Long loginCount;
}