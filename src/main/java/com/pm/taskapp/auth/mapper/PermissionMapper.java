package com.pm.taskapp.auth.mapper;

import com.pm.taskapp.auth.dto.PermissionResponseDTO;
import com.pm.taskapp.auth.enitity.Permission;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Permission entity to DTOs.
 */
@Component
public class PermissionMapper {

    /**
     * Convert Permission entity to PermissionResponseDTO.
     */
    public PermissionResponseDTO toResponseDTO(Permission permission) {
        if (permission == null) {
            return null;
        }

        PermissionResponseDTO dto = PermissionResponseDTO.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .description(permission.getDescription())
                .build();

        // Extract category from code (e.g., USER_CREATE -> USER)
        if (permission.getCode() != null && permission.getCode().contains("_")) {
            dto.setCategory(permission.getCode().substring(0, permission.getCode().indexOf("_")));
        }

        // Set role count if available
        if (permission.getRoles() != null) {
            dto.setRoleCount((long) permission.getRoles().size());
        }

        return dto;
    }

    /**
     * Convert Permission entity to simple PermissionResponseDTO.
     */
    public PermissionResponseDTO toSimpleResponseDTO(Permission permission) {
        if (permission == null) {
            return null;
        }

        return new PermissionResponseDTO(
                permission.getId(),
                permission.getCode(),
                permission.getDescription()
        );
    }
}