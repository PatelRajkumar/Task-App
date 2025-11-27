package com.pm.taskapp.auth.controller;

import com.pm.taskapp.auth.dto.PermissionCreateDTO;
import com.pm.taskapp.auth.dto.PermissionResponseDTO;
import com.pm.taskapp.auth.dto.PermissionUpdateDTO;
import com.pm.taskapp.auth.service.PermissionService;
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
 * REST controller for permission management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "Permission management APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Get all permissions.
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_READ')")
    @Operation(summary = "Get all permissions", description = "Retrieve all available permissions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<PermissionResponseDTO>> getAllPermissions() {
        log.debug("Getting all permissions");
        List<PermissionResponseDTO> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    /**
     * Get permission by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_READ')")
    @Operation(summary = "Get permission by ID", description = "Get permission details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permission found",
                content = @Content(schema = @Schema(implementation = PermissionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Permission not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PermissionResponseDTO> getPermissionById(
            @PathVariable @Parameter(description = "Permission ID") UUID id) {
        
        log.debug("Getting permission by ID: {}", id);
        PermissionResponseDTO permission = permissionService.findById(id);
        return ResponseEntity.ok(permission);
    }

    /**
     * Get permission by code.
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_READ')")
    @Operation(summary = "Get permission by code", description = "Get permission details by code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permission found",
                content = @Content(schema = @Schema(implementation = PermissionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Permission not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PermissionResponseDTO> getPermissionByCode(
            @PathVariable @Parameter(description = "Permission code") String code) {
        
        log.debug("Getting permission by code: {}", code);
        PermissionResponseDTO permission = permissionService.findByCode(code);
        return ResponseEntity.ok(permission);
    }

    /**
     * Get permissions by category.
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_READ')")
    @Operation(summary = "Get permissions by category", description = "Get all permissions in a category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permissions retrieved"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<PermissionResponseDTO>> getPermissionsByCategory(
            @PathVariable String category) {
        
        log.debug("Getting permissions for category: {}", category);
        List<PermissionResponseDTO> permissions = permissionService.getPermissionsByCategory(category);
        return ResponseEntity.ok(permissions);
    }

    /**
     * Create new permission.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_CREATE')")
    @Operation(summary = "Create permission", description = "Create a new permission")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Permission created successfully",
                content = @Content(schema = @Schema(implementation = PermissionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Permission already exists"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PermissionResponseDTO> createPermission(
            @Valid @RequestBody PermissionCreateDTO createDTO) {
        
        log.info("Creating new permission: {}", createDTO.getCode());
        PermissionResponseDTO newPermission = permissionService.createPermission(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPermission);
    }

    /**
     * Bulk create permissions.
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_CREATE')")
    @Operation(summary = "Bulk create permissions", description = "Create multiple permissions at once")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Permissions created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<PermissionResponseDTO>> bulkCreatePermissions(
            @Valid @RequestBody List<PermissionCreateDTO> createDTOs) {
        
        log.info("Bulk creating {} permissions", createDTOs.size());
        List<PermissionResponseDTO> permissions = permissionService.bulkCreatePermissions(createDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(permissions);
    }

    /**
     * Update permission.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_UPDATE')")
    @Operation(summary = "Update permission", description = "Update permission information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permission updated successfully",
                content = @Content(schema = @Schema(implementation = PermissionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Permission not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PermissionResponseDTO> updatePermission(
            @PathVariable UUID id,
            @Valid @RequestBody PermissionUpdateDTO updateDTO) {
        
        log.info("Updating permission: {}", id);
        PermissionResponseDTO updatedPermission = permissionService.updatePermission(id, updateDTO);
        return ResponseEntity.ok(updatedPermission);
    }

    /**
     * Delete permission.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_DELETE')")
    @Operation(summary = "Delete permission", description = "Delete a permission")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Permission deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Permission not found"),
        @ApiResponse(responseCode = "400", description = "Cannot delete system permission or permission in use"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deletePermission(@PathVariable UUID id) {
        log.warn("Deleting permission: {}", id);
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get permissions by role.
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_READ')")
    @Operation(summary = "Get permissions by role", description = "Get all permissions for a role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permissions retrieved"),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<PermissionResponseDTO>> getPermissionsByRole(@PathVariable UUID roleId) {
        log.debug("Getting permissions for role: {}", roleId);
        List<PermissionResponseDTO> permissions = permissionService.getPermissionsByRole(roleId);
        return ResponseEntity.ok(permissions);
    }

    /**
     * Get permissions by user.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_READ') or #userId == authentication.principal.id")
    @Operation(summary = "Get permissions by user", description = "Get all permissions for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permissions retrieved"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Set<PermissionResponseDTO>> getPermissionsByUser(@PathVariable UUID userId) {
        log.debug("Getting permissions for user: {}", userId);
        Set<PermissionResponseDTO> permissions = permissionService.getPermissionsByUser(userId);
        return ResponseEntity.ok(permissions);
    }

    /**
     * Check if permission exists.
     */
    @GetMapping("/exists/{code}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PERMISSION_READ')")
    @Operation(summary = "Check permission exists", description = "Check if permission exists by code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Existence check result"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ExistsResponse> checkPermissionExists(@PathVariable String code) {
        log.debug("Checking if permission exists: {}", code);
        boolean exists = permissionService.existsByCode(code);
        return ResponseEntity.ok(new ExistsResponse(exists));
    }

    /**
     * Check if user has permission.
     */
    @GetMapping("/check")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == authentication.principal.id")
    @Operation(summary = "Check user permission", description = "Check if user has specific permission")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permission check result"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PermissionCheckResponse> checkUserPermission(
            @RequestParam UUID userId,
            @RequestParam String permissionCode) {
        
        log.debug("Checking if user {} has permission {}", userId, permissionCode);
        boolean hasPermission = permissionService.userHasPermission(userId, permissionCode);
        return ResponseEntity.ok(new PermissionCheckResponse(hasPermission));
    }

    // Response DTOs

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExistsResponse {
        private boolean exists;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PermissionCheckResponse {
        private boolean hasPermission;
    }
}