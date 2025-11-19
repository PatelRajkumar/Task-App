package com.pm.taskapp.auth.repository;

import com.pm.taskapp.auth.enitity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Find by role name (e.g. ROLE_ADMIN, ROLE_USER).
     */
    Optional<Role> findByName(String name);

    /**
     * Load role with permissions to avoid later lazy-fetch.
     */
    @EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findWithPermissionsByName(String name);
}

