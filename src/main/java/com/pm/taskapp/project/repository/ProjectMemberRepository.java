package com.pm.taskapp.project.repository;

import com.pm.taskapp.project.entity.Project;
import com.pm.taskapp.project.entity.ProjectMember;
import com.pm.taskapp.project.enums.ProjectRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ProjectMember entity.
 * Manages project membership and role-based queries.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Membership lookup and validation</li>
 *   <li>Role-based member queries</li>
 *   <li>Member management operations</li>
 *   <li>Project-user relationship queries</li>
 * </ul>
 * 
 * <p>Important Notes:
 * <ul>
 *   <li>Enforce unique constraint: one user per project</li>
 *   <li>Respect role hierarchy in queries</li>
 *   <li>Use EntityGraph to avoid N+1 queries</li>
 *   <li>Database trigger protects last owner removal</li>
 * </ul>
 * 
 * @see ProjectMember
 * @see Project
 * @see ProjectRole
 * @since 1.0.0
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    // ========== Basic Membership Queries ==========

    /**
     * Check if user is a member of project.
     * Most efficient way to check membership without loading full entities.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return true if user is a member
     */
    boolean existsByProject_IdAndUser_Id(UUID projectId, UUID userId);

    /**
     * Find member by project and user.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return Optional containing member if found
     */
    Optional<ProjectMember> findByProject_IdAndUser_Id(UUID projectId, UUID userId);

    /**
     * Find member by project and user with user details loaded.
     * Uses EntityGraph to eagerly load user to avoid lazy loading issues.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return Optional containing member with user loaded
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT pm FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    Optional<ProjectMember> findByProject_IdAndUser_IdWithUser(
        @Param("projectId") UUID projectId,
        @Param("userId") UUID userId
    );

    // ========== List Members Queries ==========

    /**
     * Find all members of a project.
     * Returns members without loading user details.
     * 
     * @param projectId Project ID
     * @return List of project members
     */
    List<ProjectMember> findByProject_Id(UUID projectId);

    /**
     * Find all members of a project with user details loaded.
     * Recommended for displaying member lists in UI.
     * 
     * @param projectId Project ID
     * @return List of project members with users loaded
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId")
    List<ProjectMember> findByProject_IdWithUsers(@Param("projectId") UUID projectId);

    /**
     * Find all members of a project ordered by role level (highest first).
     * 
     * @param projectId Project ID
     * @return List of members sorted by role hierarchy
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT pm FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId " +
           "ORDER BY " +
           "CASE pm.role " +
           "  WHEN 'OWNER' THEN 4 " +
           "  WHEN 'ADMIN' THEN 3 " +
           "  WHEN 'MEMBER' THEN 2 " +
           "  WHEN 'VIEWER' THEN 1 " +
           "END DESC, pm.joinedAt ASC")
    List<ProjectMember> findByProject_IdOrderedByRole(@Param("projectId") UUID projectId);

    /**
     * Find all projects where user is a member.
     * 
     * @param userId User ID
     * @return List of user's project memberships
     */
    List<ProjectMember> findByUser_Id(UUID userId);

    /**
     * Find all projects where user is a member with project details loaded.
     * 
     * @param userId User ID
     * @return List of memberships with projects loaded
     */
    @EntityGraph(attributePaths = {"project"})
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.user.id = :userId")
    List<ProjectMember> findByUser_IdWithProjects(@Param("userId") UUID userId);

    // ========== Role-Based Queries ==========

    /**
     * Find member with specific role in project.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @param role Project role
     * @return Optional containing member if found with that role
     */
    Optional<ProjectMember> findByProject_IdAndUser_IdAndRole(
        UUID projectId,
        UUID userId,
        ProjectRole role
    );

    /**
     * Find all members with specific role in project.
     * 
     * @param projectId Project ID
     * @param role Project role to filter by
     * @return List of members with given role
     */
    List<ProjectMember> findByProject_IdAndRole(UUID projectId, ProjectRole role);

    /**
     * Find all members with specific role in project with user details.
     * 
     * @param projectId Project ID
     * @param role Project role
     * @return List of members with users loaded
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT pm FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId AND pm.role = :role")
    List<ProjectMember> findByProject_IdAndRoleWithUsers(
        @Param("projectId") UUID projectId,
        @Param("role") ProjectRole role
    );

    /**
     * Find all projects where user has specific role.
     * 
     * @param userId User ID
     * @param role Project role
     * @return List of projects where user has given role
     */
    @Query("SELECT pm.project FROM ProjectMember pm " +
           "WHERE pm.user.id = :userId AND pm.role = :role")
    List<Project> findProjectsByUser_IdAndRole(
        @Param("userId") UUID userId,
        @Param("role") ProjectRole role
    );

    /**
     * Find all OWNER members across all projects (admin query).
     * 
     * @param pageable Pagination parameters
     * @return Page of owner memberships
     */
    @EntityGraph(attributePaths = {"project", "user"})
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.role = 'OWNER'")
    Page<ProjectMember> findAllOwners(Pageable pageable);

    // ========== Count Queries ==========

    /**
     * Count members in a project.
     * 
     * @param projectId Project ID
     * @return Number of members
     */
    long countByProject_Id(UUID projectId);

    /**
     * Count members with specific role in project.
     * Critical for enforcing "at least one owner" rule.
     * 
     * @param projectId Project ID
     * @param role Project role
     * @return Number of members with given role
     */
    long countByProject_IdAndRole(UUID projectId, ProjectRole role);

    /**
     * Count projects where user is a member.
     * 
     * @param userId User ID
     * @return Number of projects user belongs to
     */
    long countByUser_Id(UUID userId);

    /**
     * Count active projects where user has specific role.
     * Only counts non-archived projects.
     * 
     * @param userId User ID
     * @param role Project role
     * @return Number of active projects with given role
     */
    @Query("SELECT COUNT(pm) FROM ProjectMember pm " +
           "WHERE pm.user.id = :userId " +
           "AND pm.role = :role " +
           "AND pm.project.isArchived = false")
    long countActiveProjectsByUser_IdAndRole(
        @Param("userId") UUID userId,
        @Param("role") ProjectRole role
    );

    // ========== Existence Checks ==========

    /**
     * Check if user is OWNER of project.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return true if user is owner
     */
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
           "FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId " +
           "AND pm.user.id = :userId " +
           "AND pm.role = 'OWNER'")
    boolean isOwner(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    /**
     * Check if user is OWNER or ADMIN of project.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return true if user is owner or admin
     */
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
           "FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId " +
           "AND pm.user.id = :userId " +
           "AND pm.role IN ('OWNER', 'ADMIN')")
    boolean isOwnerOrAdmin(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    /**
     * Check if user has at least MEMBER role (can create issues).
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return true if user is owner, admin, or member
     */
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
           "FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId " +
           "AND pm.user.id = :userId " +
           "AND pm.role IN ('OWNER', 'ADMIN', 'MEMBER')")
    boolean canPerformActions(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    // ========== Delete Operations ==========

    /**
     * Delete member by project and user.
     * <b>WARNING:</b> Database trigger will prevent deletion if this is the last owner.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return Number of deleted records (0 or 1)
     */
    @Modifying
    @Query("DELETE FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    int deleteByProject_IdAndUser_Id(
        @Param("projectId") UUID projectId,
        @Param("userId") UUID userId
    );

    /**
     * Delete all members of a project.
     * Typically called when project is being deleted (cascade handles this).
     * 
     * @param projectId Project ID
     * @return Number of deleted members
     */
    @Modifying
    @Query("DELETE FROM ProjectMember pm WHERE pm.project.id = :projectId")
    int deleteByProject_Id(@Param("projectId") UUID projectId);

    /**
     * Delete all memberships for a user.
     * Use with caution - typically for user account deletion.
     * 
     * @param userId User ID
     * @return Number of deleted memberships
     */
    @Modifying
    @Query("DELETE FROM ProjectMember pm WHERE pm.user.id = :userId")
    int deleteByUser_Id(@Param("userId") UUID userId);

    // ========== Search and Filter ==========

    /**
     * Find members by user name or email pattern.
     * Useful for member search within a project.
     * 
     * @param projectId Project ID
     * @param searchTerm Search term for name or email
     * @return List of matching members
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT pm FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId " +
           "AND (LOWER(pm.user.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "     OR LOWER(pm.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<ProjectMember> searchMembersByNameOrEmail(
        @Param("projectId") UUID projectId,
        @Param("searchTerm") String searchTerm
    );

    /**
     * Find members who joined after a specific date.
     * 
     * @param projectId Project ID
     * @param pageable Pagination parameters
     * @return Page of recent members
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT pm FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId " +
           "ORDER BY pm.joinedAt DESC")
    Page<ProjectMember> findRecentMembers(
        @Param("projectId") UUID projectId,
        Pageable pageable
    );

    // ========== Role Management Support ==========

    /**
     * Find all members that a given role can manage.
     * OWNER can manage all, ADMIN can manage MEMBER and VIEWER.
     * 
     * @param projectId Project ID
     * @param managerRole Role of the manager (OWNER or ADMIN)
     * @return List of manageable members
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT pm FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId " +
           "AND (:managerRole = 'OWNER' OR pm.role IN ('MEMBER', 'VIEWER'))")
    List<ProjectMember> findManageableMembersByRole(
        @Param("projectId") UUID projectId,
        @Param("managerRole") ProjectRole managerRole
    );

    /**
     * Check if project has at least one owner.
     * Should always return true due to database trigger enforcement.
     * 
     * @param projectId Project ID
     * @return true if project has at least one owner
     */
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
           "FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId AND pm.role = 'OWNER'")
    boolean hasOwner(@Param("projectId") UUID projectId);

    /**
     * Get user's role in project.
     * Returns null if user is not a member.
     * 
     * @param projectId Project ID
     * @param userId User ID
     * @return Project role or null
     */
    @Query("SELECT pm.role FROM ProjectMember pm " +
           "WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    Optional<ProjectRole> findUserRoleInProject(
        @Param("projectId") UUID projectId,
        @Param("userId") UUID userId
    );

    // ========== Batch Operations ==========

    /**
     * Find all members across multiple projects.
     * Useful for bulk operations.
     * 
     * @param projectIds List of project IDs
     * @return List of all members from given projects
     */
    @EntityGraph(attributePaths = {"user", "project"})
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id IN :projectIds")
    List<ProjectMember> findByProjectIdIn(@Param("projectIds") List<UUID> projectIds);

    /**
     * Find all memberships for multiple users.
     * 
     * @param userIds List of user IDs
     * @return List of all memberships for given users
     */
    @EntityGraph(attributePaths = {"project", "user"})
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.user.id IN :userIds")
    List<ProjectMember> findByUserIdIn(@Param("userIds") List<UUID> userIds);
}