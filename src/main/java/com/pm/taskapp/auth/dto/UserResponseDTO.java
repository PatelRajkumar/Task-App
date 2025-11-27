package com.pm.taskapp.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for user response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {

    private UUID id;

    private String email;

    private String name;

    private String avatarUrl;

    private boolean enabled;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant lastLoginAt;

    /**
     * User's roles.
     */
    private Set<RoleResponseDTO> roles;

    /**
     * User's effective permissions (aggregated from all roles).
     * This field is optional and populated only when needed.
     */
    private Set<PermissionResponseDTO> permissions;

    /**
     * Additional metadata.
     */
    private UserMetadataDTO metadata;

    /**
     * Simplified constructor for basic user info.
     */
    public UserResponseDTO(UUID id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }
}