package com.pm.taskapp.auth.service.impl;

import com.pm.taskapp.auth.dto.PermissionCreateDTO;
import com.pm.taskapp.auth.dto.PermissionResponseDTO;
import com.pm.taskapp.auth.dto.PermissionUpdateDTO;
import com.pm.taskapp.auth.enitity.Permission;
import com.pm.taskapp.auth.enitity.Role;
import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.auth.exception.DuplicateResourceException;
import com.pm.taskapp.auth.exception.ResourceNotFoundException;
import com.pm.taskapp.auth.mapper.PermissionMapper;
import com.pm.taskapp.auth.repository.PermissionRepository;
import com.pm.taskapp.auth.repository.RoleRepository;
import com.pm.taskapp.auth.repository.UserRepository;
import com.pm.taskapp.auth.service.PermissionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of PermissionService for permission management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionMapper permissionMapper;

    private static final String PERMISSION_NOT_FOUND = "Permission not found with id: ";
    private static final String PERMISSION_CODE_NOT_FOUND = "Permission not found with code: ";

    @Override
    public PermissionResponseDTO createPermission(PermissionCreateDTO permissionCreateDTO) {
        log.info("Creating new permission: {}", permissionCreateDTO.getCode());

        // Check if permission already exists
        if (permissionRepository.findByCode(permissionCreateDTO.getCode()).isPresent()) {
            throw new DuplicateResourceException("Permission already exists with code: " + permissionCreateDTO.getCode());
        }

        Permission permission = Permission.builder()
                .code(permissionCreateDTO.getCode().toUpperCase())
                .description(permissionCreateDTO.getDescription())
                .build();

        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission created successfully: {}", savedPermission.getCode());

        return permissionMapper.toResponseDTO(savedPermission);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponseDTO findById(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PERMISSION_NOT_FOUND + id));
        return permissionMapper.toResponseDTO(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponseDTO findByCode(String code) {
        Permission permission = getPermissionByCode(code);
        return permissionMapper.toResponseDTO(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public Permission getPermissionByCode(String code) {
        return permissionRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(PERMISSION_CODE_NOT_FOUND + code));
    }

    @Override
    public PermissionResponseDTO updatePermission(UUID id, PermissionUpdateDTO permissionUpdateDTO) {
        log.info("Updating permission: {}", id);

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PERMISSION_NOT_FOUND + id));

        // Update fields if provided
        if (permissionUpdateDTO.getCode() != null) {
            // Check if new code already exists
            Optional<Permission> existingPermission = permissionRepository.findByCode(permissionUpdateDTO.getCode());
            if (existingPermission.isPresent() && !existingPermission.get().getId().equals(id)) {
                throw new DuplicateResourceException("Permission already exists with code: " + permissionUpdateDTO.getCode());
            }
            permission.setCode(permissionUpdateDTO.getCode().toUpperCase());
        }

        if (permissionUpdateDTO.getDescription() != null) {
            permission.setDescription(permissionUpdateDTO.getDescription());
        }

        Permission updatedPermission = permissionRepository.save(permission);
        log.info("Permission updated successfully: {}", updatedPermission.getCode());

        return permissionMapper.toResponseDTO(updatedPermission);
    }

    @Override
    public void deletePermission(UUID id) {
        log.info("Deleting permission: {}", id);

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PERMISSION_NOT_FOUND + id));

        // Check if permission is assigned to any roles
        if (!permission.getRoles().isEmpty()) {
            throw new IllegalStateException("Cannot delete permission that is assigned to roles");
        }

        permissionRepository.delete(permission);
        log.info("Permission deleted successfully: {}", permission.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponseDTO> getAllPermissions() {
        log.debug("Fetching all permissions");
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toResponseDTO)
                .sorted(Comparator.comparing(PermissionResponseDTO::getCode))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponseDTO> getPermissionsByRole(UUID roleId) {
        log.debug("Fetching permissions for role: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        return role.getPermissions().stream()
                .map(permissionMapper::toResponseDTO)
                .sorted(Comparator.comparing(PermissionResponseDTO::getCode))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PermissionResponseDTO> getPermissionsByUser(UUID userId) {
        log.debug("Fetching permissions for user: {}", userId);

        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Set<PermissionResponseDTO> permissions = new HashSet<>();
        for (Role role : user.getRoles()) {
            Role roleWithPermissions = roleRepository.findWithPermissionsByName(role.getName())
                    .orElse(role);
            permissions.addAll(
                    roleWithPermissions.getPermissions().stream()
                            .map(permissionMapper::toResponseDTO)
                            .collect(Collectors.toSet())
            );
        }

        return permissions;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return permissionRepository.findByCode(code.toUpperCase()).isPresent();
    }

    @Override
    @PostConstruct
    public void createDefaultPermissions() {
        log.info("Creating default permissions if not exist");

        // User management permissions
        createPermissionIfNotExists("USER_CREATE", "Create new users");
        createPermissionIfNotExists("USER_READ", "View user details");
        createPermissionIfNotExists("USER_UPDATE", "Update user information");
        createPermissionIfNotExists("USER_DELETE", "Delete users");

        // Role management permissions
        createPermissionIfNotExists("ROLE_CREATE", "Create new roles");
        createPermissionIfNotExists("ROLE_READ", "View role details");
        createPermissionIfNotExists("ROLE_UPDATE", "Update role information");
        createPermissionIfNotExists("ROLE_DELETE", "Delete roles");
        createPermissionIfNotExists("ROLE_ASSIGN", "Assign roles to users");

        // Permission management permissions
        createPermissionIfNotExists("PERMISSION_CREATE", "Create new permissions");
        createPermissionIfNotExists("PERMISSION_READ", "View permission details");
        createPermissionIfNotExists("PERMISSION_UPDATE", "Update permission information");
        createPermissionIfNotExists("PERMISSION_DELETE", "Delete permissions");
        createPermissionIfNotExists("PERMISSION_ASSIGN", "Assign permissions to roles");

        // Task management permissions (example domain permissions)
        createPermissionIfNotExists("TASK_CREATE", "Create new tasks");
        createPermissionIfNotExists("TASK_READ", "View task details");
        createPermissionIfNotExists("TASK_UPDATE", "Update task information");
        createPermissionIfNotExists("TASK_DELETE", "Delete tasks");
        createPermissionIfNotExists("TASK_ASSIGN", "Assign tasks to users");

        // Project management permissions
        createPermissionIfNotExists("PROJECT_CREATE", "Create new projects");
        createPermissionIfNotExists("PROJECT_READ", "View project details");
        createPermissionIfNotExists("PROJECT_UPDATE", "Update project information");
        createPermissionIfNotExists("PROJECT_DELETE", "Delete projects");
        createPermissionIfNotExists("PROJECT_MANAGE", "Full project management");

        // System permissions
        createPermissionIfNotExists("SYSTEM_ADMIN", "Full system administration");
        createPermissionIfNotExists("AUDIT_READ", "View audit logs");
        createPermissionIfNotExists("SETTINGS_MANAGE", "Manage system settings");

        log.info("Default permissions created/verified");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponseDTO> getPermissionsByCategory(String category) {
        log.debug("Fetching permissions by category: {}", category);

        String prefix = category.toUpperCase() + "_";

        return permissionRepository.findAll().stream()
                .filter(permission -> permission.getCode().startsWith(prefix))
                .map(permissionMapper::toResponseDTO)
                .sorted(Comparator.comparing(PermissionResponseDTO::getCode))
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionResponseDTO> bulkCreatePermissions(List<PermissionCreateDTO> permissionDTOs) {
        log.info("Bulk creating {} permissions", permissionDTOs.size());

        List<Permission> permissions = new ArrayList<>();

        for (PermissionCreateDTO dto : permissionDTOs) {
            // Skip if already exists
            if (permissionRepository.findByCode(dto.getCode()).isEmpty()) {
                Permission permission = Permission.builder()
                        .code(dto.getCode().toUpperCase())
                        .description(dto.getDescription())
                        .build();
                permissions.add(permission);
            }
        }

        List<Permission> savedPermissions = permissionRepository.saveAll(permissions);
        log.info("Bulk created {} permissions", savedPermissions.size());

        return savedPermissions.stream()
                .map(permissionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userHasPermission(UUID userId, String permissionCode) {
        log.debug("Checking if user {} has permission {}", userId, permissionCode);

        Set<PermissionResponseDTO> userPermissions = getPermissionsByUser(userId);
        return userPermissions.stream()
                .anyMatch(permission -> permission.getCode().equals(permissionCode.toUpperCase()));
    }

    // Helper methods

    private void createPermissionIfNotExists(String code, String description) {
        if (!permissionRepository.findByCode(code).isPresent()) {
            Permission permission = Permission.builder()
                    .code(code)
                    .description(description)
                    .build();
            permissionRepository.save(permission);
            log.info("Created default permission: {}", code);
        }
    }
}