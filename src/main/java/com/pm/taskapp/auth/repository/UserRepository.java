package com.pm.taskapp.auth.repository;

import com.pm.taskapp.auth.enitity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email (case-insensitive).
     * Used for login/auth flows.
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Check existence quickly.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Load user with roles eagerly to avoid lazy-loading inside auth code.
     * EntityGraph will use the JPA mapping 'roles' to fetch in one query.
     */
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithRolesById(UUID id);

    /**
     * Useful for authentication when you need user + roles + (optionally) permissions.
     * If you later need permissions too, extend attributePaths accordingly.
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findWithRolesAndPermissionsByEmailIgnoreCase(String email);

    /**
     * Paging support to list users filtered by role name.
     * Spring Data resolves the nested property `roles.name`.
     */
    Page<User> findAllByRoles_Name(String roleName, Pageable pageable);
}