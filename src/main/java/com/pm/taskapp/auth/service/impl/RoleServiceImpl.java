package com.pm.taskapp.auth.service.impl;

import com.pm.taskapp.auth.dto.RoleCreateDTO;
import com.pm.taskapp.auth.dto.RoleResponseDTO;
import com.pm.taskapp.auth.dto.RoleUpdateDTO;
import com.pm.taskapp.auth.enitity.Permission;
import com.pm.taskapp.auth.enitity.Role;
import com.pm.taskapp.auth.exception.DuplicateResourceException;
import com.pm.taskapp.auth.exception.ResourceNotFoundException;
import com.pm.taskapp.auth.mapper.RoleMapper;
import com.pm.taskapp.auth.repository.PermissionRepository;
import com.pm.taskapp.auth.repository.RoleRepository;
import com.pm.taskapp.auth.repository.UserRepository;
import com.pm.taskapp.auth.service.RoleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of RoleService for role management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    private static final String ROLE_NOT_FOUND = "Role not found with id: ";
    private static final String ROLE_NAME_NOT_FOUND = "Role not found with name: ";
    private static final String PERMISSION_NOT_FOUND = "Permission not found with code: ";

    @Override
    public RoleResponseDTO createRole(RoleCreateDTO roleCreateDTO) {
        log.info("Creating new role: {}", roleCreateDTO.getName());

        // Check if role already exists
        if (roleRepository.findByName(roleCreateDTO.getName()).isPresent()) {
            throw new DuplicateResourceException("Role already exists with name: " + roleCreateDTO.getName());
        }

        // Create role
        Role role = Role.builder()
                .name(roleCreateDTO.getName().toUpperCase())
                .description(roleCreateDTO.getDescription())
                .build();

        // Assign permissions if provided
        if (roleCreateDTO.getPermissionCodes() != null && !roleCreateDTO.getPermissionCodes().isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            for (String permissionCode : roleCreateDTO.getPermissionCodes()) {
                Permission permission = permissionRepository.findByCode(permissionCode)
                        .orElseThrow(() -> new ResourceNotFoundException(PERMISSION_NOT_FOUND + permissionCode));
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully: {}", savedRole.getName());

        return roleMapper.toResponseDTO(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDTO findById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND + id));
        return roleMapper.toResponseDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDTO findByName(String name) {
        Role role = getRoleByName(name);
        return roleMapper.toResponseDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NAME_NOT_FOUND + name));
    }

    @Override
    public RoleResponseDTO updateRole(UUID id, RoleUpdateDTO roleUpdateDTO) {
        log.info("Updating role: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND + id));

        // Update fields if provided
        if (roleUpdateDTO.getName() != null) {
            // Check if new name already exists
            Optional<Role> existingRole = roleRepository.findByName(roleUpdateDTO.getName());
            if (existingRole.isPresent() && !existingRole.get().getId().equals(id)) {
                throw new DuplicateResourceException("Role already exists with name: " + roleUpdateDTO.getName());
            }
            role.setName(roleUpdateDTO.getName().toUpperCase());
        }

        if (roleUpdateDTO.getDescription() != null) {
            role.setDescription(roleUpdateDTO.getDescription());
        }

        Role updatedRole = roleRepository.save(role);
        log.info("Role updated successfully: {}", updatedRole.getName());

        return roleMapper.toResponseDTO(updatedRole);
    }

    @Override
    public void deleteRole(UUID id) {
        log.info("Deleting role: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND + id));

        // Check if role is assigned to any users
        if (!role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role that is assigned to users");
        }

        // Prevent deletion of system roles
        if (isSystemRole(role.getName())) {
            throw new IllegalStateException("Cannot delete system role: " + role.getName());
        }

        roleRepository.delete(role);
        log.info("Role deleted successfully: {}", role.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDTO> getAllRoles() {
        log.debug("Fetching all roles");
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponseDTO assignPermissions(UUID roleId, Set<String> permissionCodes) {
        log.info("Assigning permissions {} to role: {}", permissionCodes, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND + roleId));

        Set<Permission> permissions = new HashSet<>();
        for (String permissionCode : permissionCodes) {
            Permission permission = permissionRepository.findByCode(permissionCode)
                    .orElseThrow(() -> new ResourceNotFoundException(PERMISSION_NOT_FOUND + permissionCode));
            permissions.add(permission);
        }

        role.getPermissions().addAll(permissions);
        Role updatedRole = roleRepository.save(role);

        log.info("Permissions assigned successfully to role: {}", roleId);
        return roleMapper.toResponseDTO(updatedRole);
    }

    @Override
    public RoleResponseDTO removePermission(UUID roleId, String permissionCode) {
        log.info("Removing permission {} from role: {}", permissionCode, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND + roleId));

        Permission permissionToRemove = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new ResourceNotFoundException(PERMISSION_NOT_FOUND + permissionCode));

        role.getPermissions().remove(permissionToRemove);
        Role updatedRole = roleRepository.save(role);

        log.info("Permission removed successfully from role: {}", roleId);
        return roleMapper.toResponseDTO(updatedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleWithPermissions(String roleName) {
        return roleRepository.findWithPermissionsByName(roleName.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NAME_NOT_FOUND + roleName));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return roleRepository.findByName(name.toUpperCase()).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDTO> getRolesByUserId(UUID userId) {
        log.debug("Fetching roles for user: {}", userId);

        return userRepository.findWithRolesById(userId)
                .map(user -> user.getRoles().stream()
                        .map(roleMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Override
    @PostConstruct
    public void createDefaultRoles() {
        log.info("Creating default roles if not exist");

        createRoleIfNotExists("ROLE_ADMIN", "Administrator role with full access");
        createRoleIfNotExists("ROLE_USER", "Default user role");
        createRoleIfNotExists("ROLE_MANAGER", "Manager role with elevated privileges");
        createRoleIfNotExists("ROLE_GUEST", "Guest role with limited access");

        log.info("Default roles created/verified");
    }

    @Override
    public String getRoleHierarchy() {
        // Define role hierarchy for Spring Security
        // Higher roles inherit permissions from lower roles
        return "ROLE_ADMIN > ROLE_MANAGER > ROLE_USER > ROLE_GUEST";
    }

    // Helper methods

    private void createRoleIfNotExists(String name, String description) {
        if (!roleRepository.findByName(name).isPresent()) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .build();
            roleRepository.save(role);
            log.info("Created default role: {}", name);
        }
    }

    private boolean isSystemRole(String roleName) {
        return Arrays.asList("ROLE_ADMIN", "ROLE_USER", "ROLE_MANAGER", "ROLE_GUEST")
                .contains(roleName.toUpperCase());
    }
}