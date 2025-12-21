package com.pm.taskapp.auth.service.impl;

import com.pm.taskapp.auth.dto.PasswordChangeDTO;
import com.pm.taskapp.auth.dto.UserCreateDTO;
import com.pm.taskapp.auth.dto.UserResponseDTO;
import com.pm.taskapp.auth.dto.UserUpdateDTO;
import com.pm.taskapp.auth.enitity.Role;
import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.auth.exception.ResourceNotFoundException;
import com.pm.taskapp.auth.exception.DuplicateResourceException;
import com.pm.taskapp.auth.exception.InvalidPasswordException;
import com.pm.taskapp.auth.mapper.UserMapper;
import com.pm.taskapp.auth.repository.RoleRepository;
import com.pm.taskapp.auth.repository.UserRepository;
import com.pm.taskapp.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of UserService for user management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final String USER_NOT_FOUND = "User not found with id: ";
    private static final String EMAIL_NOT_FOUND = "User not found with email: ";
    private static final String ROLE_NOT_FOUND = "Role not found: ";

    @Override
    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {
        log.info("Creating new user with email: {}", userCreateDTO.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(userCreateDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + userCreateDTO.getEmail());
        }

        // Create user entity
        User user = User.builder()
                .email(userCreateDTO.getEmail().toLowerCase())
                .name(userCreateDTO.getName())
                .avatarUrl(userCreateDTO.getAvatarUrl())
                .passwordHash(passwordEncoder.encode(userCreateDTO.getPassword()))
                .enabled(true)
                .build();

        // Assign default role
        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND + DEFAULT_ROLE));
        user.getRoles().add(defaultRole);

        // Assign additional roles if provided
        if (userCreateDTO.getRoleNames() != null && !userCreateDTO.getRoleNames().isEmpty()) {
            for (String roleName : userCreateDTO.getRoleNames()) {
                if (!roleName.equals(DEFAULT_ROLE)) {
                    Role role = roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND + roleName));
                    user.getRoles().add(role);
                }
            }
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());

        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        User user = findUserById(id);
        return userMapper.toResponseDTOWithPermissions(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserById(UUID id) {
        return userRepository.findWithRolesById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findByEmail(String email) {
        User user = findUserByEmail(email);
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userRepository.findWithRolesAndPermissionsByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(EMAIL_NOT_FOUND + email));
    }

    @Override
    public UserResponseDTO updateUser(UUID id, UserUpdateDTO updateDTO) {
        log.info("Updating user with id: {}", id);

        User user = findUserById(id);

        // Update fields if provided
        if (updateDTO.getName() != null) {
            user.setName(updateDTO.getName());
        }
        if (updateDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDTO.getAvatarUrl());
        }
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equalsIgnoreCase(user.getEmail())) {
            // Check if new email already exists
            if (userRepository.existsByEmailIgnoreCase(updateDTO.getEmail())) {
                throw new DuplicateResourceException("Email already exists: " + updateDTO.getEmail());
            }
            user.setEmail(updateDTO.getEmail().toLowerCase());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getId());

        return userMapper.toResponseDTO(updatedUser);
    }

    @Override
    public void changePassword(UUID userId, PasswordChangeDTO passwordChangeDTO) {
        log.info("Changing password for user: {}", userId);

        User user = findUserById(userId);

        // Verify old password
        if (!passwordEncoder.matches(passwordChangeDTO.getOldPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        // Validate new password
        if (passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getOldPassword())) {
            throw new InvalidPasswordException("New password must be different from current password");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    public void resetPassword(UUID userId, String newPassword) {
        log.info("Resetting password for user: {}", userId);

        User user = findUserById(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", userId);
    }

    @Override
    public void setUserEnabled(UUID userId, boolean enabled) {
        log.info("Setting user enabled status to {} for user: {}", enabled, userId);

        User user = findUserById(userId);
        user.setEnabled(enabled);
        userRepository.save(user);

        log.info("User enabled status updated for user: {}", userId);
    }

    @Override
    public void deleteUser(UUID id) {
        log.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(USER_NOT_FOUND + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        return userRepository.findAll(pageable)
                .map(userMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getUsersByRole(String roleName, Pageable pageable) {
        log.debug("Fetching users with role: {}", roleName);
        return userRepository.findAllByRoles_Name(roleName, pageable)
                .map(userMapper::toResponseDTO);
    }

    @Override
    public UserResponseDTO assignRoles(UUID userId, Set<String> roleNames) {
        log.info("Assigning roles {} to user: {}", roleNames, userId);

        User user = findUserById(userId);
        Set<Role> newRoles = new HashSet<>();

        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND + roleName));
            newRoles.add(role);
        }

        user.getRoles().addAll(newRoles);
        User updatedUser = userRepository.save(user);

        log.info("Roles assigned successfully to user: {}", userId);
        return userMapper.toResponseDTO(updatedUser);
    }

    @Override
    public UserResponseDTO removeRole(UUID userId, String roleName) {
        log.info("Removing role {} from user: {}", roleName, userId);

        User user = findUserById(userId);
        Role roleToRemove = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND + roleName));

        // Prevent removing the last role
        if (user.getRoles().size() == 1 && user.getRoles().contains(roleToRemove)) {
            throw new IllegalStateException("Cannot remove the last role from user");
        }

        user.getRoles().remove(roleToRemove);
        User updatedUser = userRepository.save(user);

        log.info("Role removed successfully from user: {}", userId);
        return userMapper.toResponseDTO(updatedUser);
    }

    @Override
    public void updateLastLogin(UUID userId) {
        log.debug("Updating last login for user: {}", userId);

        User user = findUserById(userId);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Searching users with term: {}", searchTerm);

        // Note: You'll need to add this method to UserRepository
        // Page<User> findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(String email, String name, Pageable pageable);

        // For now, returning all users as placeholder
        return userRepository.findAll(pageable)
                .map(userMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserWithRolesAndPermissions(UUID userId) {
        return userRepository.findWithRolesAndPermissionsByEmailIgnoreCase(
                findUserById(userId).getEmail()
        ).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + userId));
    }
}