package com.pm.taskapp.auth.mapper;

import com.pm.taskapp.auth.dto.RoleResponseDTO;
import com.pm.taskapp.auth.dto.PermissionResponseDTO;
import com.pm.taskapp.auth.enitity.Role;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting Role entity to DTOs.
 */
@Component
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    /**
     * Convert Role entity to RoleResponseDTO.
     */
    public RoleResponseDTO toResponseDTO(Role role) {
        if (role == null) {
            return null;
        }

        RoleResponseDTO dto = RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();

        // Map permissions if present
        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            dto.setPermissions(role.getPermissions().stream()
                    .map(permissionMapper::toResponseDTO)
                    .collect(Collectors.toSet()));
        }

        // Set user count if available
        if (role.getUsers() != null) {
            dto.setUserCount((long) role.getUsers().size());
        }

        return dto;
    }

    /**
     * Convert Role entity to simple RoleResponseDTO (without permissions).
     */
    public RoleResponseDTO toSimpleResponseDTO(Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}