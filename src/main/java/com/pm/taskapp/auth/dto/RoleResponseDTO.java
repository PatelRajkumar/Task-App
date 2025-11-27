package com.pm.taskapp.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Set;
import java.util.UUID;

/**
 * DTO for role response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResponseDTO {

    private UUID id;

    private String name;

    private String description;

    /**
     * Permissions assigned to this role.
     */
    private Set<PermissionResponseDTO> permissions;

    /**
     * Number of users with this role.
     */
    private Long userCount;

    /**
     * Whether this is a system role.
     */
    private boolean systemRole;

    /**
     * Role priority in hierarchy.
     */
    private int priority;

    /**
     * Simplified constructor for basic role info.
     */
    public RoleResponseDTO(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}