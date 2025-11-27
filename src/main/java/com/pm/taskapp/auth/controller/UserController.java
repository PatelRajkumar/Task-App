package com.pm.taskapp.auth.controller;

import com.pm.taskapp.auth.dto.*;
import com.pm.taskapp.auth.security.CurrentUser;
import com.pm.taskapp.auth.security.UserPrincipal;
import com.pm.taskapp.auth.service.UserService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

/**
 * REST controller for user management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user's profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponseDTO> getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        log.debug("Getting current user profile: {}", userPrincipal.getId());
        UserResponseDTO user = userService.findById(userPrincipal.getId());
        return ResponseEntity.ok(user);
    }

    /**
     * Update current user profile.
     */
    @PutMapping("/me")
    @Operation(summary = "Update current user", description = "Update current user's profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        
        log.info("Updating current user profile: {}", userPrincipal.getId());
        UserResponseDTO updatedUser = userService.updateUser(userPrincipal.getId(), updateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Change current user password.
     */
    @PostMapping("/me/change-password")
    @Operation(summary = "Change password", description = "Change current user's password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid old password or weak new password"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MessageResponse> changePassword(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        
        log.info("Password change request for user: {}", userPrincipal.getId());
        userService.changePassword(userPrincipal.getId(), passwordChangeDTO);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    /**
     * Delete current user account.
     */
    @DeleteMapping("/me")
    @Operation(summary = "Delete account", description = "Delete current user's account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteCurrentUser(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(required = false) String confirmPassword) {
        
        log.warn("Account deletion requested for user: {}", userPrincipal.getId());
        userService.deleteUser(userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all users (Admin only).
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('USER_READ')")
    @Operation(summary = "Get all users", description = "Get paginated list of all users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("Getting all users, page: {}", pageable.getPageNumber());
        Page<UserResponseDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID (Admin only).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('USER_READ')")
    @Operation(summary = "Get user by ID", description = "Get user details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable @Parameter(description = "User ID") UUID id) {
        
        log.debug("Getting user by ID: {}", id);
        UserResponseDTO user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Create new user (Admin only).
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('USER_CREATE')")
    @Operation(summary = "Create user", description = "Create a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Email already exists"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        log.info("Creating new user: {}", createDTO.getEmail());
        UserResponseDTO newUser = userService.createUser(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    /**
     * Update user (Admin only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('USER_UPDATE')")
    @Operation(summary = "Update user", description = "Update user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        
        log.info("Updating user: {}", id);
        UserResponseDTO updatedUser = userService.updateUser(id, updateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user (Admin only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('USER_DELETE')")
    @Operation(summary = "Delete user", description = "Delete user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        log.warn("Deleting user: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Enable/disable user account (Admin only).
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('USER_UPDATE')")
    @Operation(summary = "Update user status", description = "Enable or disable user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status updated"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<MessageResponse> updateUserStatus(
            @PathVariable UUID id,
            @RequestParam boolean enabled) {
        
        log.info("Updating user status: {} to enabled={}", id, enabled);
        userService.setUserEnabled(id, enabled);
        String status = enabled ? "enabled" : "disabled";
        return ResponseEntity.ok(new MessageResponse("User account " + status + " successfully"));
    }

    /**
     * Assign roles to user (Admin only).
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('ROLE_ASSIGN')")
    @Operation(summary = "Assign roles", description = "Assign roles to user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Roles assigned successfully",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User or role not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserResponseDTO> assignRoles(
            @PathVariable UUID id,
            @RequestBody Set<String> roleNames) {
        
        log.info("Assigning roles {} to user: {}", roleNames, id);
        UserResponseDTO user = userService.assignRoles(id, roleNames);
        return ResponseEntity.ok(user);
    }

    /**
     * Remove role from user (Admin only).
     */
    @DeleteMapping("/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('ROLE_ASSIGN')")
    @Operation(summary = "Remove role", description = "Remove role from user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role removed successfully",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User or role not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserResponseDTO> removeRole(
            @PathVariable UUID id,
            @PathVariable String roleName) {
        
        log.info("Removing role {} from user: {}", roleName, id);
        UserResponseDTO user = userService.removeRole(id, roleName);
        return ResponseEntity.ok(user);
    }

    /**
     * Search users (Admin only).
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('USER_READ')")
    @Operation(summary = "Search users", description = "Search users by name or email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<UserResponseDTO>> searchUsers(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Searching users with query: {}", query);
        Page<UserResponseDTO> results = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(results);
    }

    /**
     * Get users by role (Admin only).
     */
    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('USER_READ')")
    @Operation(summary = "Get users by role", description = "Get all users with specific role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<UserResponseDTO>> getUsersByRole(
            @PathVariable String roleName,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting users with role: {}", roleName);
        Page<UserResponseDTO> users = userService.getUsersByRole(roleName, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Reset user password (Admin only).
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Reset user password", description = "Admin reset of user password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PasswordResetResponse> resetUserPassword(@PathVariable UUID id) {
        log.warn("Admin password reset for user: {}", id);
        
        // Generate temporary password
        String tempPassword = generateTemporaryPassword();
        userService.resetPassword(id, tempPassword);
        
        return ResponseEntity.ok(new PasswordResetResponse(tempPassword, 
            "Temporary password generated. User must change it on next login."));
    }

    // Helper methods and DTOs

    private String generateTemporaryPassword() {
        // Generate secure temporary password
        return UUID.randomUUID().toString().substring(0, 12) + "Aa1!";
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MessageResponse {
        private String message;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PasswordResetResponse {
        private String temporaryPassword;
        private String message;
    }
}