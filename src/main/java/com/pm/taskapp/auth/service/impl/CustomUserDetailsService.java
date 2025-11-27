package com.pm.taskapp.auth.service.impl;

import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.auth.exception.ResourceNotFoundException;
import com.pm.taskapp.auth.repository.UserRepository;
import com.pm.taskapp.auth.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * Loads user details for authentication and authorization.
 */
@Slf4j
@Service("customUserDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username (email in our case).
     * This method is called by Spring Security during authentication.
     *
     * @param email User email
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository.findWithRolesAndPermissionsByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.debug("User found: {}, enabled: {}", user.getEmail(), user.isEnabled());

        if (!user.isEnabled()) {
            log.warn("User account is disabled: {}", email);
        }

        return UserPrincipal.create(user);
    }

    /**
     * Load user by ID.
     * This method is used by JWT authentication filter.
     *
     * @param id User ID
     * @return UserDetails for Spring Security
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        log.debug("Loading user by id: {}", id);

        User user = userRepository.findWithRolesAndPermissionsByEmailIgnoreCase(
                userRepository.findById(id)
                        .orElseThrow(() -> {
                            log.error("User not found with id: {}", id);
                            return new ResourceNotFoundException("User not found with id: " + id);
                        }).getEmail()
        ).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return UserPrincipal.create(user);
    }

    /**
     * Check if user exists by email.
     *
     * @param email User email
     * @return true if exists
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Check if user is enabled.
     *
     * @param email User email
     * @return true if enabled
     */
    @Transactional(readOnly = true)
    public boolean isUserEnabled(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(User::isEnabled)
                .orElse(false);
    }

    /**
     * Get user entity by email.
     *
     * @param email User email
     * @return User entity
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findWithRolesAndPermissionsByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}