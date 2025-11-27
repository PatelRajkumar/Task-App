package com.pm.taskapp.auth.mapper;

import com.pm.taskapp.auth.dto.UserResponseDTO;
import com.pm.taskapp.auth.dto.RoleResponseDTO;
import com.pm.taskapp.auth.dto.PermissionResponseDTO;
import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.auth.enitity.Role;
import com.pm.taskapp.auth.enitity.Permission;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting User entity to DTOs.
 */
@Component
public class UserMapper {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public UserMapper(RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    /**
     * Convert User entity to UserResponseDTO.
     */
    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDTO dto = UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();

        // Map roles if present
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            dto.setRoles(user.getRoles().stream()
                    .map(roleMapper::toResponseDTO)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    /**
     * Convert User entity to UserResponseDTO with permissions.
     */
    public UserResponseDTO toResponseDTOWithPermissions(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDTO dto = toResponseDTO(user);

        // Aggregate all permissions from user's roles
        Set<Permission> allPermissions = new HashSet<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                if (role.getPermissions() != null) {
                    allPermissions.addAll(role.getPermissions());
                }
            }
        }

        // Map permissions
        if (!allPermissions.isEmpty()) {
            dto.setPermissions(allPermissions.stream()
                    .map(permissionMapper::toResponseDTO)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    /**
     * Convert User entity to simple UserResponseDTO (without roles).
     */
    public UserResponseDTO toSimpleResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .enabled(user.isEnabled())
                .build();
    }
}