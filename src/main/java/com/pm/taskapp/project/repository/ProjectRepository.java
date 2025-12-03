package com.pm.taskapp.project.repository;

import com.pm.taskapp.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Project entity.
 * Provides CRUD operations and custom queries for project management.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Basic CRUD operations (inherited from JpaRepository)</li>
 *   <li>Custom queries for project search and filtering</li>
 *   <li>User-specific project queries</li>
 *   <li>Visibility-based access control queries</li>
 *   <li>Support for pagination and sorting</li>
 * </ul>
 * 
 * <p>Query Patterns:
 * <ul>
 *   <li>Find by key/ID with optional member loading</li>
 *   <li>Find projects where user is a member</li>
 *   <li>Search projects by name or key</li>
 *   <li>Filter by visibility and archive status</li>
 * </ul>
 * 
 * @see Project
 * @since 1.0.0
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // ========== Basic Find Operations ==========

    /**
     * Find project by unique key.
     * 
     * @param key Project key (e.g., "PROJ-1")
     * @return Optional containing project if found
     */
    Optional<Project> findByKey(String key);

    /**
     * Find project by key with members eagerly loaded.
     * Useful when you need project details with member information.
     * Uses EntityGraph to avoid N+1 query problem.
     * 
     * @param key Project key
     * @return Optional containing project with members loaded
     */
    @EntityGraph(attributePaths = {"members", "members.user"})
    @Query("SELECT p FROM Project p WHERE p.key = :key")
    Optional<Project> findByKeyWithMembers(@Param("key") String key);

    /**
     * Find project by ID with members eagerly loaded.
     * 
     * @param id Project ID
     * @return Optional containing project with members loaded
     */
    @EntityGraph(attributePaths = {"members", "members.user"})
    @Query("SELECT p FROM Project p WHERE p.id = :id")
    Optional<Project> findByIdWithMembers(@Param("id") UUID id);

    /**
     * Check if project exists by key.
     * More efficient than findByKey() when you only need to check existence.
     * 
     * @param key Project key
     * @return true if project exists
     */
    boolean existsByKey(String key);

    // ========== User-Specific Queries ==========

    /**
     * Find all active projects where user is a member.
     * Excludes archived projects.
     * 
     * @param userId User ID
     * @param pageable Pagination and sorting parameters
     * @return Page of projects
     */
    @Query("SELECT DISTINCT p FROM Project p " +
           "JOIN p.members pm " +
           "WHERE pm.user.id = :userId " +
           "AND p.isArchived = false " +
           "ORDER BY p.updatedAt DESC")
    Page<Project> findUserProjects(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find all projects where user is a member (including archived).
     * 
     * @param userId User ID
     * @param pageable Pagination and sorting parameters
     * @return Page of projects
     */
    @Query("SELECT DISTINCT p FROM Project p " +
           "JOIN p.members pm " +
           "WHERE pm.user.id = :userId " +
           "ORDER BY p.updatedAt DESC")
    Page<Project> findAllUserProjects(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find archived projects where user is OWNER.
     * Only OWNERs can view archived projects.
     * 
     * @param userId User ID
     * @param pageable Pagination and sorting parameters
     * @return Page of archived projects owned by user
     */
    @Query("SELECT p FROM Project p " +
           "JOIN p.members pm " +
           "WHERE pm.user.id = :userId " +
           "AND pm.role = 'OWNER' " +
           "AND p.isArchived = true " +
           "ORDER BY p.archivedAt DESC")
    Page<Project> findArchivedProjectsOwnedBy(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Count active projects where user is a member.
     * 
     * @param userId User ID
     * @return Number of active projects
     */
    @Query("SELECT COUNT(DISTINCT p) FROM Project p " +
           "JOIN p.members pm " +
           "WHERE pm.user.id = :userId " +
           "AND p.isArchived = false")
    long countUserProjects(@Param("userId") UUID userId);

    /**
     * Count total projects where user is a member (including archived).
     * 
     * @param userId User ID
     * @return Total number of projects
     */
    @Query("SELECT COUNT(DISTINCT p) FROM Project p " +
           "JOIN p.members pm " +
           "WHERE pm.user.id = :userId")
    long countAllUserProjects(@Param("userId") UUID userId);

    // ========== Search and Filter Queries ==========

    /**
     * Search projects by name or key that user can access.
     * Access rules:
     * - PUBLIC projects: Anyone can see
     * - PRIVATE projects: Only members can see
     * - Archived projects: Only OWNERs can see
     * 
     * @param searchTerm Search term (matched against name and key)
     * @param userId User ID for access control
     * @param pageable Pagination and sorting parameters
     * @return Page of matching projects
     */
    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN p.members pm " +
           "WHERE (p.visibility = 'PUBLIC' " +
           "       OR (pm.user.id = :userId AND p.isArchived = false)) " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "     OR LOWER(p.key) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY p.updatedAt DESC")
    Page<Project> searchProjects(
        @Param("searchTerm") String searchTerm,
        @Param("userId") UUID userId,
        Pageable pageable
    );

    /**
     * Find all public active projects.
     * Useful for showing public project directory.
     * 
     * @param pageable Pagination and sorting parameters
     * @return Page of public projects
     */
    @Query("SELECT p FROM Project p " +
           "WHERE p.visibility = 'PUBLIC' " +
           "AND p.isArchived = false " +
           "ORDER BY p.createdAt DESC")
    Page<Project> findPublicProjects(Pageable pageable);

    /**
     * Find all active projects (admin use).
     * Should only be accessible by system administrators.
     * 
     * @param pageable Pagination and sorting parameters
     * @return Page of all active projects
     */
    @Query("SELECT p FROM Project p " +
           "WHERE p.isArchived = false " +
           "ORDER BY p.createdAt DESC")
    Page<Project> findAllActiveProjects(Pageable pageable);

    /**
     * Find projects created by a specific user.
     * 
     * @param creatorId User ID of project creator
     * @param pageable Pagination and sorting parameters
     * @return Page of projects created by user
     */
    @Query("SELECT p FROM Project p " +
           "WHERE p.createdBy.id = :creatorId " +
           "ORDER BY p.createdAt DESC")
    Page<Project> findProjectsByCreator(@Param("creatorId") UUID creatorId, Pageable pageable);

    // ========== Project Key Generation Support ==========

    /**
     * Get the next project key from database function.
     * Uses the PostgreSQL function created in V8 migration.
     * Returns format: PROJ-{number}
     * 
     * <p>Example: PROJ-1, PROJ-2, PROJ-3, etc.
     * 
     * @return Next available project key
     */
    @Query(value = "SELECT get_next_project_key()", nativeQuery = true)
    String getNextProjectKey();

    /**
     * Find the maximum sequential number from existing PROJ-{n} keys.
     * Fallback method if database function is not available.
     * 
     * @return Maximum number, or null if no projects exist
     */
    @Query("SELECT MAX(CAST(SUBSTRING(p.key, 6) AS int)) FROM Project p " +
           "WHERE p.key LIKE 'PROJ-%'")
    Integer findMaxProjectNumber();

    // ========== Statistics and Analytics ==========

    /**
     * Count projects by visibility.
     * 
     * @param visibility Project visibility (PUBLIC or PRIVATE)
     * @return Number of projects with given visibility
     */
    long countByVisibility(@Param("visibility") String visibility);

    /**
     * Count archived projects.
     * 
     * @return Number of archived projects
     */
    long countByIsArchivedTrue();

    /**
     * Count active projects.
     * 
     * @return Number of active (non-archived) projects
     */
    long countByIsArchivedFalse();

    /**
     * Find recently updated projects that user can access.
     * Shows recent activity across accessible projects.
     * 
     * @param userId User ID for access control
     * @param pageable Pagination parameters (typically small page size)
     * @return Page of recently updated projects
     */
    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN p.members pm " +
           "WHERE (p.visibility = 'PUBLIC' OR pm.user.id = :userId) " +
           "AND p.isArchived = false " +
           "ORDER BY p.updatedAt DESC")
    Page<Project> findRecentlyUpdatedProjects(
        @Param("userId") UUID userId,
        Pageable pageable
    );

    /**
     * Find projects with names matching pattern (case-insensitive).
     * 
     * @param namePattern Name pattern to match
     * @param pageable Pagination and sorting parameters
     * @return Page of matching projects
     */
    Page<Project> findByNameContainingIgnoreCase(String namePattern, Pageable pageable);

    /**
     * Find active projects by creator.
     * 
     * @param creatorId Creator user ID
     * @param pageable Pagination and sorting parameters
     * @return Page of active projects created by user
     */
    @Query("SELECT p FROM Project p " +
           "WHERE p.createdBy.id = :creatorId " +
           "AND p.isArchived = false " +
           "ORDER BY p.createdAt DESC")
    Page<Project> findActiveProjectsByCreator(
        @Param("creatorId") UUID creatorId,
        Pageable pageable
    );

    // ========== Batch Operations ==========

    /**
     * Find projects by IDs.
     * Useful for batch operations.
     * 
     * @param ids List of project IDs
     * @return List of projects
     */
    List<Project> findByIdIn(List<UUID> ids);

    /**
     * Find projects by keys.
     * Useful for batch operations.
     * 
     * @param keys List of project keys
     * @return List of projects
     */
    List<Project> findByKeyIn(List<String> keys);

    /**
     * Delete projects by creator.
     * Use with extreme caution - typically for test cleanup only.
     * 
     * @param creatorId Creator user ID
     * @return Number of deleted projects
     */
    long deleteByCreatedBy_Id(UUID creatorId);
}