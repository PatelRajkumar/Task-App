package com.pm.taskapp.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

/**
 * DTO for session metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionMetadataDTO {

    /**
     * Session ID.
     */
    private String sessionId;

    /**
     * IP address of the session.
     */
    private String ipAddress;

    /**
     * User agent string.
     */
    private String userAgent;

    /**
     * Device type (mobile, desktop, tablet).
     */
    private String deviceType;

    /**
     * Browser name.
     */
    private String browser;

    /**
     * Operating system.
     */
    private String operatingSystem;

    /**
     * Geographic location (city, country).
     */
    private String location;

    /**
     * Session creation time.
     */
    private Instant createdAt;

    /**
     * Session expiration time.
     */
    private Instant expiresAt;

    /**
     * Whether this is a trusted device.
     */
    private boolean trustedDevice;

    /**
     * Risk score (0-100, higher means more risky).
     */
    private Integer riskScore;
}