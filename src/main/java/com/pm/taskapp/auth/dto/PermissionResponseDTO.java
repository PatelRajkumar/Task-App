package com.pm.taskapp.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.UUID;

/**
 * DTO for permission response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionResponseDTO {

    private UUID id;

    private String code;

    private String description;

    /**
     * Category or module this permission belongs to.
     */
    private String category;

    /**
     * Number of roles that have this permission.
     */
    private Long roleCount;

    /**
     * Whether this is a system permission.
     */
    private boolean systemPermission;

    /**
     * Simplified constructor for basic permission info.
     */
    public PermissionResponseDTO(UUID id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }
}