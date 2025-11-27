package com.pm.taskapp.auth.service;

import com.pm.taskapp.auth.dto.UserCreateDTO;
import com.pm.taskapp.auth.dto.UserResponseDTO;
import com.pm.taskapp.auth.dto.UserUpdateDTO;
import com.pm.taskapp.auth.dto.PasswordChangeDTO;
import com.pm.taskapp.auth.enitity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for user management operations.
 */
public interface UserService {

    /**
     * Create a new user with encrypted password and default role.
     *
     * @param userCreateDTO User creation data
     * @return Created user response
     */
    UserResponseDTO createUser(UserCreateDTO userCreateDTO);

    /**
     * Find user by ID.
     *
     * @param id User ID
     * @return User response
     */
    UserResponseDTO findById(UUID id);

    /**
     * Find user entity by ID (internal use).
     *
     * @param id User ID
     * @return User entity
     */
    User findUserById(UUID id);

    /**
     * Find user by email.
     *
     * @param email User email
     * @return User response
     */
    UserResponseDTO findByEmail(String email);

    /**
     * Find user entity by email (internal use).
     *
     * @param email User email
     * @return User entity
     */
    User findUserByEmail(String email);

    /**
     * Update user profile information.
     *
     * @param id User ID
     * @param updateDTO Update data
     * @return Updated user response
     */
    UserResponseDTO updateUser(UUID id, UserUpdateDTO updateDTO);

    /**
     * Change user password.
     *
     * @param userId User ID
     * @param passwordChangeDTO Password change data
     */
    void changePassword(UUID userId, PasswordChangeDTO passwordChangeDTO);

    /**
     * Reset user password (admin operation).
     *
     * @param userId User ID
     * @param newPassword New password
     */
    void resetPassword(UUID userId, String newPassword);

    /**
     * Enable or disable user account.
     *
     * @param userId User ID
     * @param enabled Enable status
     */
    void setUserEnabled(UUID userId, boolean enabled);

    /**
     * Delete user by ID.
     *
     * @param id User ID
     */
    void deleteUser(UUID id);

    /**
     * Check if email already exists.
     *
     * @param email Email to check
     * @return true if exists
     */
    boolean existsByEmail(String email);

    /**
     * Get all users with pagination.
     *
     * @param pageable Pagination parameters
     * @return Page of users
     */
    Page<UserResponseDTO> getAllUsers(Pageable pageable);

    /**
     * Get users by role with pagination.
     *
     * @param roleName Role name
     * @param pageable Pagination parameters
     * @return Page of users
     */
    Page<UserResponseDTO> getUsersByRole(String roleName, Pageable pageable);

    /**
     * Assign roles to user.
     *
     * @param userId User ID
     * @param roleNames Set of role names
     * @return Updated user
     */
    UserResponseDTO assignRoles(UUID userId, Set<String> roleNames);

    /**
     * Remove role from user.
     *
     * @param userId User ID
     * @param roleName Role name to remove
     * @return Updated user
     */
    UserResponseDTO removeRole(UUID userId, String roleName);

    /**
     * Update last login timestamp.
     *
     * @param userId User ID
     */
    void updateLastLogin(UUID userId);

    /**
     * Search users by name or email.
     *
     * @param searchTerm Search term
     * @param pageable Pagination parameters
     * @return Page of matching users
     */
    Page<UserResponseDTO> searchUsers(String searchTerm, Pageable pageable);

    /**
     * Validate user password.
     *
     * @param user User entity
     * @param rawPassword Raw password to validate
     * @return true if password matches
     */
    boolean validatePassword(User user, String rawPassword);

    /**
     * Get user with all roles and permissions loaded.
     *
     * @param userId User ID
     * @return User with roles and permissions
     */
    User getUserWithRolesAndPermissions(UUID userId);
}