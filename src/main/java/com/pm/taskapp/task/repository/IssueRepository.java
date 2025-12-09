package com.pm.taskapp.task.repository;

import com.pm.taskapp.task.entity.Issue;
import com.pm.taskapp.task.enums.IssuePriority;
import com.pm.taskapp.task.enums.IssueStatus;
import com.pm.taskapp.task.enums.IssueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Issue entity.
 * Provides CRUD operations and custom queries for issue/task management.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Basic CRUD operations (inherited from JpaRepository)</li>
 *   <li>Custom queries for filtering by status, type, priority, assignee</li>
 *   <li>Project-specific issue queries</li>
 *   <li>Search functionality by title/description</li>
 *   <li>Soft delete support (excludes deleted issues by default)</li>
 *   <li>Support for pagination and sorting</li>
 * </ul>
 *
 * <p>Query Patterns:
 * <ul>
 *   <li>Find by key/ID with optional relationship loading</li>
 *   <li>Find issues within a project with various filters</li>
 *   <li>Find issues assigned to or reported by specific users</li>
 *   <li>Search issues by text content</li>
 *   <li>Filter by status, type, priority</li>
 *   <li>All queries automatically exclude soft-deleted issues unless explicitly requested</li>
 * </ul>
 *
 * @see Issue
 * @since 1.0.0
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {

    // ========== Basic Find Operations ==========

    /**
     * Find issue by unique key.
     * Excludes soft-deleted issues.
     *
     * @param key Issue key (e.g., "PROJ-123")
     * @return Optional containing issue if found and not deleted
     */
    @Query("SELECT i FROM Issue i WHERE i.key = :key AND i.isDeleted = false")
    Optional<Issue> findByKey(@Param("key") String key);

    /**
     * Find issue by ID.
     * Excludes soft-deleted issues.
     *
     * @param id Issue ID
     * @return Optional containing issue if found and not deleted
     */
    @Query("SELECT i FROM Issue i WHERE i.id = :id AND i.isDeleted = false")
    Optional<Issue> findById(@Param("id") UUID id);

    /**
     * Find issue by key with project, reporter, and assignee eagerly loaded.
     * Useful when you need complete issue details.
     * Uses EntityGraph to avoid N+1 query problem.
     * Excludes soft-deleted issues.
     *
     * @param key Issue key
     * @return Optional containing issue with relationships loaded
     */
    @EntityGraph(attributePaths = {"project", "reporter", "assignee"})
    @Query("SELECT i FROM Issue i WHERE i.key = :key AND i.isDeleted = false")
    Optional<Issue> findByKeyWithDetails(@Param("key") String key);

    /**
     * Find issue by ID with project, reporter, and assignee eagerly loaded.
     * Excludes soft-deleted issues.
     *
     * @param id Issue ID
     * @return Optional containing issue with relationships loaded
     */
    @EntityGraph(attributePaths = {"project", "reporter", "assignee"})
    @Query("SELECT i FROM Issue i WHERE i.id = :id AND i.isDeleted = false")
    Optional<Issue> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Check if issue exists by key (including deleted issues).
     *
     * @param key Issue key
     * @return true if issue exists
     */
    boolean existsByKey(String key);

    /**
     * Check if active (non-deleted) issue exists by key.
     * More efficient than findByKey() when you only need to check existence.
     *
     * @param key Issue key
     * @return true if active issue exists
     */
    @Query("SELECT COUNT(i) > 0 FROM Issue i WHERE i.key = :key AND i.isDeleted = false")
    boolean existsByKeyAndNotDeleted(@Param("key") String key);

    // ========== Project-Specific Queries ==========

    /**
     * Find all active issues in a project.
     * Excludes soft-deleted issues.
     * Ordered by creation date (newest first).
     *
     * @param projectId Project ID
     * @param pageable Pagination and sorting parameters
     * @return Page of active issues
     */
    @EntityGraph(attributePaths = {"assignee", "reporter", "project"})
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> findByProject(@Param("projectId") UUID projectId, Pageable pageable);

    /**
     * Find issues in a project filtered by status.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param status Issue status
     * @param pageable Pagination and sorting parameters
     * @return Page of matching issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.status = :status " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> findByProjectAndStatus(
            @Param("projectId") UUID projectId,
            @Param("status") IssueStatus status,
            Pageable pageable
    );

    /**
     * Find issues in a project filtered by type.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param type Issue type
     * @param pageable Pagination and sorting parameters
     * @return Page of matching issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.type = :type " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> findByProjectAndType(
            @Param("projectId") UUID projectId,
            @Param("type") IssueType type,
            Pageable pageable
    );

    /**
     * Find issues in a project filtered by priority.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param priority Issue priority
     * @param pageable Pagination and sorting parameters
     * @return Page of matching issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.priority = :priority " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> findByProjectAndPriority(
            @Param("projectId") UUID projectId,
            @Param("priority") IssuePriority priority,
            Pageable pageable
    );

    /**
     * Count active issues in a project.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @return Number of active issues
     */
    @Query("SELECT COUNT(i) FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.isDeleted = false")
    long countByProject(@Param("projectId") UUID projectId);

    /**
     * Count issues in a project by status.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param status Issue status
     * @return Number of matching issues
     */
    @Query("SELECT COUNT(i) FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.status = :status " +
            "AND i.isDeleted = false")
    long countByProjectAndStatus(
            @Param("projectId") UUID projectId,
            @Param("status") IssueStatus status
    );

    // ========== User-Specific Queries ==========

    /**
     * Find issues assigned to a specific user.
     * Excludes soft-deleted issues.
     *
     * @param assigneeId User ID of assignee
     * @param pageable Pagination and sorting parameters
     * @return Page of assigned issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.assignee.id = :assigneeId " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> findByAssignee(@Param("assigneeId") UUID assigneeId, Pageable pageable);

    /**
     * Find issues reported by a specific user.
     * Excludes soft-deleted issues.
     *
     * @param reporterId User ID of reporter
     * @param pageable Pagination and sorting parameters
     * @return Page of reported issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.reporter.id = :reporterId " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> findByReporter(@Param("reporterId") UUID reporterId, Pageable pageable);

    /**
     * Find issues in a project assigned to a specific user.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param assigneeId User ID of assignee
     * @param pageable Pagination and sorting parameters
     * @return Page of assigned issues in project
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.assignee.id = :assigneeId " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> findByProjectAndAssignee(
            @Param("projectId") UUID projectId,
            @Param("assigneeId") UUID assigneeId,
            Pageable pageable
    );

    /**
     * Find unassigned issues in a project.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param pageable Pagination and sorting parameters
     * @return Page of unassigned issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.assignee IS NULL " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> findUnassignedByProject(@Param("projectId") UUID projectId, Pageable pageable);

    /**
     * Count issues assigned to a user in a project.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param assigneeId User ID of assignee
     * @return Number of assigned issues
     */
    @Query("SELECT COUNT(i) FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.assignee.id = :assigneeId " +
            "AND i.isDeleted = false")
    long countByProjectAndAssignee(
            @Param("projectId") UUID projectId,
            @Param("assigneeId") UUID assigneeId
    );

    // ========== Search and Filter Queries ==========

    /**
     * Search issues in a project by title or description (case-insensitive).
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param searchTerm Search term to match against title/description
     * @param pageable Pagination and sorting parameters
     * @return Page of matching issues
     */
    @EntityGraph(attributePaths = {"assignee", "reporter", "project"})
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND (LOWER(i.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "     OR LOWER(i.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> searchInProject(
            @Param("projectId") UUID projectId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    /**
     * Advanced filter: Find issues by multiple criteria.
     * Excludes soft-deleted issues.
     * All filter parameters are optional (nullable).
     *
     * @param projectId Project ID (required)
     * @param status Issue status (optional)
     * @param type Issue type (optional)
     * @param priority Issue priority (optional)
     * @param assigneeId Assignee user ID (optional)
     * @param reporterId Reporter user ID (optional)
     * @param pageable Pagination and sorting parameters
     * @return Page of matching issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND (:status IS NULL OR i.status = :status) " +
            "AND (:type IS NULL OR i.type = :type) " +
            "AND (:priority IS NULL OR i.priority = :priority) " +
            "AND (:assigneeId IS NULL OR i.assignee.id = :assigneeId) " +
            "AND (:reporterId IS NULL OR i.reporter.id = :reporterId) " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> findByFilters(
            @Param("projectId") UUID projectId,
            @Param("status") IssueStatus status,
            @Param("type") IssueType type,
            @Param("priority") IssuePriority priority,
            @Param("assigneeId") UUID assigneeId,
            @Param("reporterId") UUID reporterId,
            Pageable pageable
    );

    /**
     * Combined search and filter.
     * Searches by text and filters by criteria.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param searchTerm Search term (optional)
     * @param status Issue status (optional)
     * @param type Issue type (optional)
     * @param priority Issue priority (optional)
     * @param assigneeId Assignee user ID (optional)
     * @param pageable Pagination and sorting parameters
     * @return Page of matching issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND (:searchTerm IS NULL OR " +
            "     LOWER(i.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(i.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:status IS NULL OR i.status = :status) " +
            "AND (:type IS NULL OR i.type = :type) " +
            "AND (:priority IS NULL OR i.priority = :priority) " +
            "AND (:assigneeId IS NULL OR i.assignee.id = :assigneeId) " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> searchAndFilter(
            @Param("projectId") UUID projectId,
            @Param("searchTerm") String searchTerm,
            @Param("status") IssueStatus status,
            @Param("type") IssueType type,
            @Param("priority") IssuePriority priority,
            @Param("assigneeId") UUID assigneeId,
            Pageable pageable
    );

    // ========== Date-Based Queries ==========

    /**
     * Find overdue issues in a project.
     * An issue is overdue if it has a due date in the past and is not DONE.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param today Current date
     * @param pageable Pagination and sorting parameters
     * @return Page of overdue issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.dueDate < :today " +
            "AND i.status != 'DONE' " +
            "AND i.isDeleted = false " +
            "ORDER BY i.dueDate ASC")
    Page<Issue> findOverdueIssues(
            @Param("projectId") UUID projectId,
            @Param("today") LocalDate today,
            Pageable pageable
    );

    /**
     * Find issues due on a specific date.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param dueDate Due date to match
     * @param pageable Pagination and sorting parameters
     * @return Page of issues due on date
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.dueDate = :dueDate " +
            "AND i.isDeleted = false " +
            "ORDER BY i.priority DESC, i.createdAt ASC")
    Page<Issue> findByDueDate(
            @Param("projectId") UUID projectId,
            @Param("dueDate") LocalDate dueDate,
            Pageable pageable
    );

    /**
     * Find issues created within a date range.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param startDate Range start (inclusive)
     * @param endDate Range end (inclusive)
     * @param pageable Pagination and sorting parameters
     * @return Page of issues created in range
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.createdAt >= :startDate " +
            "AND i.createdAt <= :endDate " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> findByCreatedAtBetween(
            @Param("projectId") UUID projectId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    /**
     * Find recently updated issues in a project.
     * Shows recent activity.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param pageable Pagination parameters (typically small page size)
     * @return Page of recently updated issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.isDeleted = false " +
            "ORDER BY i.updatedAt DESC")
    Page<Issue> findRecentlyUpdated(@Param("projectId") UUID projectId, Pageable pageable);

    // ========== Status-Based Queries ==========

    /**
     * Find open (not DONE) issues in a project.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param pageable Pagination and sorting parameters
     * @return Page of open issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.status != 'DONE' " +
            "AND i.isDeleted = false " +
            "ORDER BY i.createdAt DESC")
    Page<Issue> findOpenIssues(@Param("projectId") UUID projectId, Pageable pageable);

    /**
     * Find resolved (DONE) issues in a project.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @param pageable Pagination and sorting parameters
     * @return Page of resolved issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.status = 'DONE' " +
            "AND i.isDeleted = false " +
            "ORDER BY i.resolvedAt DESC")
    Page<Issue> findResolvedIssues(@Param("projectId") UUID projectId, Pageable pageable);

    /**
     * Count open (not DONE) issues in a project.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @return Number of open issues
     */
    @Query("SELECT COUNT(i) FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.status != 'DONE' " +
            "AND i.isDeleted = false")
    long countOpenIssues(@Param("projectId") UUID projectId);

    // ========== Soft Delete Queries ==========

    /**
     * Find soft-deleted issues in a project.
     * For admin/recovery purposes only.
     *
     * @param projectId Project ID
     * @param pageable Pagination and sorting parameters
     * @return Page of deleted issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.isDeleted = true " +
            "ORDER BY i.deletedAt DESC")
    Page<Issue> findDeletedIssues(@Param("projectId") UUID projectId, Pageable pageable);

    /**
     * Find all issues deleted by a specific user.
     * For audit purposes.
     *
     * @param deletedById User ID who deleted the issues
     * @param pageable Pagination and sorting parameters
     * @return Page of deleted issues
     */
    @Query("SELECT i FROM Issue i " +
            "WHERE i.deletedBy.id = :deletedById " +
            "AND i.isDeleted = true " +
            "ORDER BY i.deletedAt DESC")
    Page<Issue> findIssuesDeletedBy(@Param("deletedById") UUID deletedById, Pageable pageable);

    // ========== Statistics and Analytics ==========

    /**
     * Get issue count breakdown by status for a project.
     * Returns list of [status, count] arrays.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @return List of status counts
     */
    @Query("SELECT i.status, COUNT(i) FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.isDeleted = false " +
            "GROUP BY i.status")
    List<Object[]> countByStatusGrouped(@Param("projectId") UUID projectId);

    /**
     * Get issue count breakdown by type for a project.
     * Returns list of [type, count] arrays.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @return List of type counts
     */
    @Query("SELECT i.type, COUNT(i) FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.isDeleted = false " +
            "GROUP BY i.type")
    List<Object[]> countByTypeGrouped(@Param("projectId") UUID projectId);

    /**
     * Get issue count breakdown by priority for a project.
     * Returns list of [priority, count] arrays.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @return List of priority counts
     */
    @Query("SELECT i.priority, COUNT(i) FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.isDeleted = false " +
            "GROUP BY i.priority " +
            "ORDER BY i.priority DESC")
    List<Object[]> countByPriorityGrouped(@Param("projectId") UUID projectId);

    /**
     * Count issues by assignee in a project.
     * Useful for workload distribution analysis.
     * Excludes soft-deleted issues.
     *
     * @param projectId Project ID
     * @return List of [assignee_id, count] arrays
     */
    @Query("SELECT i.assignee.id, COUNT(i) FROM Issue i " +
            "WHERE i.project.id = :projectId " +
            "AND i.assignee IS NOT NULL " +
            "AND i.isDeleted = false " +
            "GROUP BY i.assignee.id " +
            "ORDER BY COUNT(i) DESC")
    List<Object[]> countByAssigneeGrouped(@Param("projectId") UUID projectId);

    // ========== Batch Operations ==========

    /**
     * Find issues by IDs.
     * Excludes soft-deleted issues.
     * Useful for batch operations.
     *
     * @param ids List of issue IDs
     * @return List of issues
     */
    @Query("SELECT i FROM Issue i WHERE i.id IN :ids AND i.isDeleted = false")
    List<Issue> findByIdIn(@Param("ids") List<UUID> ids);

    /**
     * Find issues by keys.
     * Excludes soft-deleted issues.
     * Useful for batch operations.
     *
     * @param keys List of issue keys
     * @return List of issues
     */
    @Query("SELECT i FROM Issue i WHERE i.key IN :keys AND i.isDeleted = false")
    List<Issue> findByKeyIn(@Param("keys") List<String> keys);
}