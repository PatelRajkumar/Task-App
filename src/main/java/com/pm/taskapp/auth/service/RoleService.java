package com.pm.taskapp.auth.service;

import com.pm.taskapp.auth.dto.RoleCreateDTO;
import com.pm.taskapp.auth.dto.RoleResponseDTO;
import com.pm.taskapp.auth.dto.RoleUpdateDTO;
import com.pm.taskapp.auth.enitity.Role;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for role management operations.
 */
public interface RoleService {

    /**
     * Create a new role.
     *
     * @param roleCreateDTO Role creation data
     * @return Created role
     */
    RoleResponseDTO createRole(RoleCreateDTO roleCreateDTO);

    /**
     * Find role by ID.
     *
     * @param id Role ID
     * @return Role response
     */
    RoleResponseDTO findById(UUID id);

    /**
     * Find role by name.
     *
     * @param name Role name
     * @return Role response
     */
    RoleResponseDTO findByName(String name);

    /**
     * Get role entity by name.
     *
     * @param name Role name
     * @return Role entity
     */
    Role getRoleByName(String name);

    /**
     * Update role.
     *
     * @param id Role ID
     * @param roleUpdateDTO Update data
     * @return Updated role
     */
    RoleResponseDTO updateRole(UUID id, RoleUpdateDTO roleUpdateDTO);

    /**
     * Delete role.
     *
     * @param id Role ID
     */
    void deleteRole(UUID id);

    /**
     * Get all roles.
     *
     * @return List of all roles
     */
    List<RoleResponseDTO> getAllRoles();

    /**
     * Assign permissions to role.
     *
     * @param roleId Role ID
     * @param permissionCodes Set of permission codes
     * @return Updated role
     */
    RoleResponseDTO assignPermissions(UUID roleId, Set<String> permissionCodes);

    /**
     * Remove permission from role.
     *
     * @param roleId Role ID
     * @param permissionCode Permission code
     * @return Updated role
     */
    RoleResponseDTO removePermission(UUID roleId, String permissionCode);

    /**
     * Get role with permissions loaded.
     *
     * @param roleName Role name
     * @return Role with permissions
     */
    Role getRoleWithPermissions(String roleName);

    /**
     * Check if role exists.
     *
     * @param name Role name
     * @return true if exists
     */
    boolean existsByName(String name);

    /**
     * Get roles by user ID.
     *
     * @param userId User ID
     * @return List of user's roles
     */
    List<RoleResponseDTO> getRolesByUserId(UUID userId);

    /**
     * Create default roles (for initialization).
     */
    void createDefaultRoles();

    /**
     * Get role hierarchy.
     *
     * @return Role hierarchy as string
     */
    String getRoleHierarchy();
}