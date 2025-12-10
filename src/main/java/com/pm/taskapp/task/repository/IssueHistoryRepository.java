package com.pm.taskapp.task.repository;

import com.pm.taskapp.task.entity.IssueHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

public interface IssueHistoryRepository extends JpaRepository<IssueHistory, UUID> {

    /**
     * Find all history records for a specific issue.
     * Ordered by change date (newest first).
     *
     * @param issueId Issue ID
     * @param pageable Pagination and sorting parameters
     * @return Page of history records
     */
    @EntityGraph(attributePaths = {"changedBy"})
    @Query("SELECT ih FROM IssueHistory ih " +
            "WHERE ih.issue.id = :issueId " +
            "ORDER BY ih.changedAt DESC")
    Page<IssueHistory> findByIssue(@Param("issueId") UUID issueId, Pageable pageable);

    /**
     * Count history records for a specific issue.
     *
     * @param issueId Issue ID
     * @return Number of history records
     */
    @Query("SELECT COUNT(ih) FROM IssueHistory ih WHERE ih.issue.id = :issueId")
    long countByIssue(@Param("issueId") UUID issueId);

    // ========== Project-Specific History ==========

    /**
     * Find all history records in a project.
     * Shows activity across all issues in the project.
     * Ordered by change date (newest first).
     *
     * @param projectId Project ID
     * @param pageable Pagination and sorting parameters
     * @return Page of history records
     */
    @EntityGraph(attributePaths = {"changedBy", "issue"})
    @Query("SELECT ih FROM IssueHistory ih " +
            "WHERE ih.issue.project.id = :projectId " +
            "ORDER BY ih.changedAt DESC")
    Page<IssueHistory> findByProject(@Param("projectId") UUID projectId, Pageable pageable);

    /**
     * Find recent activity in a project.
     * Returns most recent changes across all issues.
     *
     * @param projectId Project ID
     * @param pageable Pagination parameters (typically small page size like 10-20)
     * @return Page of recent history records
     */
    @EntityGraph(attributePaths = {"changedBy", "issue"})
    @Query("SELECT ih FROM IssueHistory ih " +
            "WHERE ih.issue.project.id = :projectId " +
            "ORDER BY ih.changedAt DESC")
    Page<IssueHistory> findRecentActivity(@Param("projectId") UUID projectId, Pageable pageable);

    // ========== Date-Based History ==========

    /**
     * Find history records for an issue within a date range.
     *
     * @param issueId Issue ID
     * @param startDate Range start (inclusive)
     * @param endDate Range end (inclusive)
     * @param pageable Pagination and sorting parameters
     * @return Page of history records
     */
    @EntityGraph(attributePaths = {"changedBy"})
    @Query("SELECT ih FROM IssueHistory ih " +
            "WHERE ih.issue.id = :issueId " +
            "AND ih.changedAt >= :startDate " +
            "AND ih.changedAt <= :endDate " +
            "ORDER BY ih.changedAt DESC")
    Page<IssueHistory> findByIssueAndDateRange(
            @Param("issueId") UUID issueId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    /**
     * Find all history records in a project within a date range.
     * Useful for generating activity reports.
     *
     * @param projectId Project ID
     * @param startDate Range start (inclusive)
     * @param endDate Range end (inclusive)
     * @param pageable Pagination and sorting parameters
     * @return Page of history records
     */
    @EntityGraph(attributePaths = {"changedBy", "issue"})
    @Query("SELECT ih FROM IssueHistory ih " +
            "WHERE ih.issue.project.id = :projectId " +
            "AND ih.changedAt >= :startDate " +
            "AND ih.changedAt <= :endDate " +
            "ORDER BY ih.changedAt DESC")
    Page<IssueHistory> findByProjectAndDateRange(
            @Param("projectId") UUID projectId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    // ========== Field-Specific History (Optional but Useful) ==========

    /**
     * Find history records for a specific field in an issue.
     * Example: Only show status changes, or only assignment changes.
     *
     * @param issueId Issue ID
     * @param field Field name (status, assignee, priority, title)
     * @param pageable Pagination and sorting parameters
     * @return Page of history records
     */
    @EntityGraph(attributePaths = {"changedBy"})
    @Query("SELECT ih FROM IssueHistory ih " +
            "WHERE ih.issue.id = :issueId " +
            "AND ih.field = :field " +
            "ORDER BY ih.changedAt DESC")
    Page<IssueHistory> findByIssueAndField(
            @Param("issueId") UUID issueId,
            @Param("field") String field,
            Pageable pageable
    );

    // ========== User Activity (Optional but Useful) ==========

    /**
     * Find all changes made by a specific user in a project.
     * Useful for user activity tracking and audit.
     *
     * @param userId User ID who made the changes
     * @param projectId Project ID
     * @param pageable Pagination and sorting parameters
     * @return Page of history records
     */
    @EntityGraph(attributePaths = {"issue"})
    @Query("SELECT ih FROM IssueHistory ih " +
            "WHERE ih.changedBy.id = :userId " +
            "AND ih.issue.project.id = :projectId " +
            "ORDER BY ih.changedAt DESC")
    Page<IssueHistory> findByUserAndProject(
            @Param("userId") UUID userId,
            @Param("projectId") UUID projectId,
            Pageable pageable
    );
}
