package com.pm.taskapp.auth.controller;

import com.pm.taskapp.auth.dto.RoleCreateDTO;
import com.pm.taskapp.auth.dto.RoleResponseDTO;
import com.pm.taskapp.auth.dto.RoleUpdateDTO;
import com.pm.taskapp.auth.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * REST controller for role management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Role management APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class RoleController {

    private final RoleService roleService;

    /**
     * Get all roles.
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('ROLE_READ')")
    @Operation(summary = "Get all roles", description = "Retrieve all available roles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<RoleResponseDTO>> getAllRoles() {
        log.debug("Getting all roles");
        List<RoleResponseDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Get role by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('ROLE_READ')")
    @Operation(summary = "Get role by ID", description = "Get role details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role found",
                content = @Content(schema = @Schema(implementation = RoleResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<RoleResponseDTO> getRoleById(
            @PathVariable @Parameter(description = "Role ID") UUID id) {
        
        log.debug("Getting role by ID: {}", id);
        RoleResponseDTO role = roleService.findById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Get role by name.
     */
    @GetMapping("/name/{name}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('ROLE_READ')")
    @Operation(summary = "Get role by name", description = "Get role details by name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role found",
                content = @Content(schema = @Schema(implementation = RoleResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<RoleResponseDTO> getRoleByName(
            @PathVariable @Parameter(description = "Role name") String name) {
        
        log.debug("Getting role by name: {}", name);
        RoleResponseDTO role = roleService.findByName(name);
        return ResponseEntity.ok(role);
    }

    /**
     * Create new role.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('ROLE_CREATE')")
    @Operation(summary = "Create role", description = "Create a new role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Role created successfully",
                content = @Content(schema = @Schema(implementation = RoleResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Role already exists"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<RoleResponseDTO> createRole(@Valid @RequestBody RoleCreateDTO createDTO) {
        log.info("Creating new role: {}", createDTO.getName());
        RoleResponseDTO newRole = roleService.createRole(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRole);
    }

    /**
     * Update role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('ROLE_UPDATE')")
    @Operation(summary = "Update role", description = "Update role information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role updated successfully",
                content = @Content(schema = @Schema(implementation = RoleResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<RoleResponseDTO> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleUpdateDTO updateDTO) {
        
        log.info("Updating role: {}", id);
        RoleResponseDTO updatedRole = roleService.updateRole(id, updateDTO);
        return ResponseEntity.ok(updatedRole);
    }

    /**
     * Delete role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('ROLE_DELETE')")
    @Operation(summary = "Delete role", description = "Delete a role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "400", description = "Cannot delete system role or role with users"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        log.warn("Deleting role: {}", id);
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assign permissions to role.
     */
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_ASSIGN')")
    @Operation(summary = "Assign permissions", description = "Assign permissions to role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permissions assigned successfully",
                content = @Content(schema = @Schema(implementation = RoleResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Role or permission not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<RoleResponseDTO> assignPermissions(
            @PathVariable UUID id,
            @RequestBody Set<String> permissionCodes) {
        
        log.info("Assigning permissions {} to role: {}", permissionCodes, id);
        RoleResponseDTO role = roleService.assignPermissions(id, permissionCodes);
        return ResponseEntity.ok(role);
    }

    /**
     * Remove permission from role.
     */
    @DeleteMapping("/{id}/permissions/{permissionCode}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_ASSIGN')")
    @Operation(summary = "Remove permission", description = "Remove permission from role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permission removed successfully",
                content = @Content(schema = @Schema(implementation = RoleResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Role or permission not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<RoleResponseDTO> removePermission(
            @PathVariable UUID id,
            @PathVariable String permissionCode) {
        
        log.info("Removing permission {} from role: {}", permissionCode, id);
        RoleResponseDTO role = roleService.removePermission(id, permissionCode);
        return ResponseEntity.ok(role);
    }

    /**
     * Get users with this role.
     */
    @GetMapping("/{id}/users")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('USER_READ')")
    @Operation(summary = "Get role users", description = "Get all users with this role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved"),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<RoleResponseDTO>> getRoleUsers(@PathVariable UUID id) {
        log.debug("Getting users for role: {}", id);
        // This would typically return UserResponseDTO list
        // Implementation depends on your requirements
        return ResponseEntity.ok(List.of());
    }

    /**
     * Get role hierarchy.
     */
    @GetMapping("/hierarchy")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get role hierarchy", description = "Get the role hierarchy configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hierarchy retrieved"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<HierarchyResponse> getRoleHierarchy() {
        log.debug("Getting role hierarchy");
        String hierarchy = roleService.getRoleHierarchy();
        return ResponseEntity.ok(new HierarchyResponse(hierarchy));
    }

    /**
     * Check if role exists.
     */
    @GetMapping("/exists/{name}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('ROLE_READ')")
    @Operation(summary = "Check role exists", description = "Check if role exists by name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Existence check result"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ExistsResponse> checkRoleExists(@PathVariable String name) {
        log.debug("Checking if role exists: {}", name);
        boolean exists = roleService.existsByName(name);
        return ResponseEntity.ok(new ExistsResponse(exists));
    }

    // Response DTOs

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HierarchyResponse {
        private String hierarchy;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExistsResponse {
        private boolean exists;
    }
}