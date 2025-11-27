package com.pm.taskapp.auth.service;

import com.pm.taskapp.auth.dto.PermissionCreateDTO;
import com.pm.taskapp.auth.dto.PermissionResponseDTO;
import com.pm.taskapp.auth.dto.PermissionUpdateDTO;
import com.pm.taskapp.auth.enitity.Permission;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for permission management operations.
 */
public interface PermissionService {

    /**
     * Create a new permission.
     *
     * @param permissionCreateDTO Permission creation data
     * @return Created permission
     */
    PermissionResponseDTO createPermission(PermissionCreateDTO permissionCreateDTO);

    /**
     * Find permission by ID.
     *
     * @param id Permission ID
     * @return Permission response
     */
    PermissionResponseDTO findById(UUID id);

    /**
     * Find permission by code.
     *
     * @param code Permission code
     * @return Permission response
     */
    PermissionResponseDTO findByCode(String code);

    /**
     * Get permission entity by code.
     *
     * @param code Permission code
     * @return Permission entity
     */
    Permission getPermissionByCode(String code);

    /**
     * Update permission.
     *
     * @param id Permission ID
     * @param permissionUpdateDTO Update data
     * @return Updated permission
     */
    PermissionResponseDTO updatePermission(UUID id, PermissionUpdateDTO permissionUpdateDTO);

    /**
     * Delete permission.
     *
     * @param id Permission ID
     */
    void deletePermission(UUID id);

    /**
     * Get all permissions.
     *
     * @return List of all permissions
     */
    List<PermissionResponseDTO> getAllPermissions();

    /**
     * Get permissions by role.
     *
     * @param roleId Role ID
     * @return List of permissions for role
     */
    List<PermissionResponseDTO> getPermissionsByRole(UUID roleId);

    /**
     * Get permissions by user.
     *
     * @param userId User ID
     * @return List of user's permissions
     */
    Set<PermissionResponseDTO> getPermissionsByUser(UUID userId);

    /**
     * Check if permission exists.
     *
     * @param code Permission code
     * @return true if exists
     */
    boolean existsByCode(String code);

    /**
     * Create default permissions (for initialization).
     */
    void createDefaultPermissions();

    /**
     * Get permissions by category/module.
     *
     * @param category Category/module name
     * @return List of permissions in category
     */
    List<PermissionResponseDTO> getPermissionsByCategory(String category);

    /**
     * Bulk create permissions.
     *
     * @param permissionDTOs List of permission creation data
     * @return List of created permissions
     */
    List<PermissionResponseDTO> bulkCreatePermissions(List<PermissionCreateDTO> permissionDTOs);

    /**
     * Check if user has permission.
     *
     * @param userId User ID
     * @param permissionCode Permission code
     * @return true if user has permission
     */
    boolean userHasPermission(UUID userId, String permissionCode);
}