package com.pm.taskapp.auth.repository;

import com.pm.taskapp.auth.enitity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /**
     * Find permission by code (e.g. ISSUE_READ).
     */
    Optional<Permission> findByCode(String code);

    // Find permissions by code prefix (for category search)
    List<Permission> findByCodeStartingWith(String prefix);

    // Find permissions by codes in bulk
    List<Permission> findByCodeIn(List<String> codes);
}
