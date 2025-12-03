package com.pm.taskapp.project.repository;

import com.pm.taskapp.project.entity.ProjectIssueCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ProjectIssueCounter entity.
 * Manages project issue counters with atomic operations support.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Thread-safe counter increment via pessimistic locking</li>
 *   <li>Counter lookup by project</li>
 *   <li>Bulk counter operations</li>
 *   <li>Counter statistics and validation</li>
 * </ul>
 * 
 * <p>Critical Usage Pattern:
 * <pre>
 * // CORRECT: Use pessimistic lock for atomic increment
 * {@code
 * ProjectIssueCounter counter = counterRepository.findByProjectIdForUpdate(projectId).get();
 * Integer nextNumber = counter.incrementAndGet();
 * counterRepository.save(counter);
 * }
 * 
 * // WRONG: Without lock, race conditions can occur
 * {@code
 * ProjectIssueCounter counter = counterRepository.findByProjectId(projectId).get();
 * Integer nextNumber = counter.incrementAndGet(); // NOT THREAD-SAFE!
 * counterRepository.save(counter);
 * }
 * </pre>
 * 
 * <p>Locking Strategy:
 * <ul>
 *   <li>Use PESSIMISTIC_WRITE when incrementing counter</li>
 *   <li>Use standard find() for read-only operations</li>
 *   <li>Lock is held until transaction commits</li>
 *   <li>Prevents concurrent increments from different transactions</li>
 * </ul>
 * 
 * @see ProjectIssueCounter
 * @since 1.0.0
 */
@Repository
public interface ProjectIssueCounterRepository extends JpaRepository<ProjectIssueCounter, UUID> {

    // ========== Basic Find Operations ==========

    /**
     * Find counter by project ID.
     * Use for read-only operations (checking current value).
     * <b>Do NOT use for incrementing</b> - use findByProjectIdForUpdate() instead.
     * 
     * @param projectId Project ID
     * @return Optional containing counter if found
     */
    Optional<ProjectIssueCounter> findByProject_Id(UUID projectId);

    /**
     * Find counter by project ID with pessimistic write lock.
     * <b>Use this method when incrementing the counter.</b>
     * 
     * <p>The pessimistic write lock ensures:
     * <ul>
     *   <li>No other transaction can read or modify this counter</li>
     *   <li>Counter increment is atomic and thread-safe</li>
     *   <li>No lost updates or race conditions</li>
     * </ul>
     * 
     * <p>Example usage:
     * <pre>
     * {@code
     * @Transactional
     * public String createIssue(UUID projectId, IssueCreateDTO dto) {
     *     // Acquire lock on counter
     *     ProjectIssueCounter counter = counterRepository
     *         .findByProjectIdForUpdate(projectId)
     *         .orElseThrow(() -> new NotFoundException("Counter not found"));
     *     
     *     // Safely increment (no other transaction can modify simultaneously)
     *     Integer issueNumber = counter.incrementAndGet();
     *     counterRepository.save(counter);
     *     
     *     // Generate issue key
     *     String issueKey = project.getKey() + "-" + issueNumber;
     *     // ... create issue with key
     * }
     * }
     * </pre>
     * 
     * @param projectId Project ID
     * @return Optional containing counter with write lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pic FROM ProjectIssueCounter pic WHERE pic.project.id = :projectId")
    Optional<ProjectIssueCounter> findByProjectIdForUpdate(@Param("projectId") UUID projectId);

    /**
     * Find counter by project ID with pessimistic read lock.
     * Use when you need consistent read without allowing concurrent modifications.
     * Allows other reads but prevents writes.
     * 
     * @param projectId Project ID
     * @return Optional containing counter with read lock
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT pic FROM ProjectIssueCounter pic WHERE pic.project.id = :projectId")
    Optional<ProjectIssueCounter> findByProjectIdForRead(@Param("projectId") UUID projectId);

    // ========== Existence Checks ==========

    /**
     * Check if counter exists for project.
     * Most efficient way to check existence without loading entity.
     * 
     * @param projectId Project ID
     * @return true if counter exists
     */
    boolean existsByProject_Id(UUID projectId);

    /**
     * Check if project has any issues (lastNumber > 0).
     * 
     * @param projectId Project ID
     * @return true if project has created issues
     */
    @Query("SELECT CASE WHEN pic.lastNumber > 0 THEN true ELSE false END " +
           "FROM ProjectIssueCounter pic WHERE pic.project.id = :projectId")
    boolean hasIssues(@Param("projectId") UUID projectId);

    // ========== Statistics and Analytics ==========

    /**
     * Get the current counter value (read-only).
     * Returns the last used number without locking.
     * 
     * @param projectId Project ID
     * @return Last issue number, or null if not found
     */
    @Query("SELECT pic.lastNumber FROM ProjectIssueCounter pic " +
           "WHERE pic.project.id = :projectId")
    Optional<Integer> getCurrentCounterValue(@Param("projectId") UUID projectId);

    /**
     * Get the next issue number preview without incrementing or locking.
     * Shows what the next issue number will be.
     * 
     * @param projectId Project ID
     * @return Next issue number (lastNumber + 1)
     */
    @Query("SELECT pic.lastNumber + 1 FROM ProjectIssueCounter pic " +
           "WHERE pic.project.id = :projectId")
    Optional<Integer> getNextIssueNumber(@Param("projectId") UUID projectId);

    /**
     * Count total issues across all projects.
     * Sum of all counter values.
     * 
     * @return Total issue count
     */
    @Query("SELECT SUM(pic.lastNumber) FROM ProjectIssueCounter pic")
    Long getTotalIssueCount();

    /**
     * Find counters with issue count greater than threshold.
     * Useful for identifying active projects.
     * 
     * @param threshold Minimum issue count
     * @return List of counters above threshold
     */
    @Query("SELECT pic FROM ProjectIssueCounter pic WHERE pic.lastNumber > :threshold")
    List<ProjectIssueCounter> findCountersAboveThreshold(@Param("threshold") Integer threshold);

    /**
     * Find counters with zero issues (unused projects).
     * 
     * @return List of counters with no issues
     */
    @Query("SELECT pic FROM ProjectIssueCounter pic WHERE pic.lastNumber = 0")
    List<ProjectIssueCounter> findUnusedCounters();

    /**
     * Get maximum issue count across all projects.
     * 
     * @return Highest issue count
     */
    @Query("SELECT MAX(pic.lastNumber) FROM ProjectIssueCounter pic")
    Optional<Integer> getMaxIssueCount();

    /**
     * Get average issue count per project.
     * 
     * @return Average issue count
     */
    @Query("SELECT AVG(pic.lastNumber) FROM ProjectIssueCounter pic")
    Optional<Double> getAverageIssueCount();

    /**
     * Count projects with active issues (lastNumber > 0).
     * 
     * @return Number of projects with issues
     */
    @Query("SELECT COUNT(pic) FROM ProjectIssueCounter pic WHERE pic.lastNumber > 0")
    long countProjectsWithIssues();

    /**
     * Count projects without issues (lastNumber = 0).
     * 
     * @return Number of projects without issues
     */
    @Query("SELECT COUNT(pic) FROM ProjectIssueCounter pic WHERE pic.lastNumber = 0")
    long countProjectsWithoutIssues();

    // ========== Batch Operations ==========

    /**
     * Find counters for multiple projects.
     * Useful for bulk operations or dashboard queries.
     * 
     * @param projectIds List of project IDs
     * @return List of counters for given projects
     */
    @Query("SELECT pic FROM ProjectIssueCounter pic WHERE pic.project.id IN :projectIds")
    List<ProjectIssueCounter> findByProject_IdIn(@Param("projectIds") List<UUID> projectIds);

    /**
     * Delete counter by project ID.
     * Typically handled by cascade delete when project is deleted.
     * 
     * @param projectId Project ID
     * @return Number of deleted counters (0 or 1)
     */
    long deleteByProject_Id(UUID projectId);

    /**
     * Delete counters for multiple projects.
     * Use with caution - typically for bulk cleanup operations.
     * 
     * @param projectIds List of project IDs
     * @return Number of deleted counters
     */
    @Query("DELETE FROM ProjectIssueCounter pic WHERE pic.project.id IN :projectIds")
    @Modifying
    long deleteByProject_IdIn(@Param("projectIds") List<UUID> projectIds);

    // ========== Validation and Maintenance ==========

    /**
     * Find counters with negative values (data corruption check).
     * Should always return empty list in healthy database.
     * 
     * @return List of corrupted counters
     */
    @Query("SELECT pic FROM ProjectIssueCounter pic WHERE pic.lastNumber < 0")
    List<ProjectIssueCounter> findInvalidCounters();

    /**
     * Find counters with unusually high values (potential issues).
     * Useful for detecting anomalies or test data.
     * 
     * @param threshold Maximum expected value
     * @return List of counters exceeding threshold
     */
    @Query("SELECT pic FROM ProjectIssueCounter pic WHERE pic.lastNumber > :threshold " +
           "ORDER BY pic.lastNumber DESC")
    List<ProjectIssueCounter> findCountersExceedingThreshold(@Param("threshold") Integer threshold);

    // ========== Custom Native Queries ==========

    /**
     * Reset counter to zero (dangerous operation).
     * <b>WARNING:</b> Only use for test cleanup or data migration.
     * Will cause duplicate issue keys if issues already exist.
     * 
     * @param projectId Project ID
     * @return Number of updated counters
     */
    @Query(value = "UPDATE project_issue_counters " +
                   "SET last_number = 0 " +
                   "WHERE project_id = :projectId",
           nativeQuery = true)
    int resetCounter(@Param("projectId") UUID projectId);

    /**
     * Get counter value using native SQL (bypass Hibernate).
     * Useful for debugging or when you need absolute latest value.
     * 
     * @param projectId Project ID
     * @return Counter value
     */
    @Query(value = "SELECT last_number FROM project_issue_counters " +
                   "WHERE project_id = :projectId",
           nativeQuery = true)
    Optional<Integer> getCounterValueNative(@Param("projectId") UUID projectId);

    /**
     * Increment counter using native SQL (atomic database operation).
     * Alternative to application-level increment with pessimistic lock.
     * Returns the NEW value after increment.
     * 
     * <p><b>Usage:</b>
     * <pre>
     * {@code
     * Integer nextNumber = counterRepository.incrementCounterNative(projectId);
     * String issueKey = projectKey + "-" + nextNumber;
     * }
     * </pre>
     * 
     * @param projectId Project ID
     * @return New counter value after increment
     */
    @Query(value = "UPDATE project_issue_counters " +
                   "SET last_number = last_number + 1 " +
                   "WHERE project_id = :projectId " +
                   "RETURNING last_number",
           nativeQuery = true)
    Integer incrementCounterNative(@Param("projectId") UUID projectId);
}