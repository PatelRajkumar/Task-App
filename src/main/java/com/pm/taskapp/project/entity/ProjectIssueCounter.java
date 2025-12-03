package com.pm.taskapp.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a project's issue counter.
 * Tracks the last used sequential number for issue generation.
 * 
 * <p>Purpose:
 * Generates unique, sequential issue numbers within a project scope.
 * For example: PROJ-1, PROJ-2, PROJ-3, etc.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>One counter per project (one-to-one relationship)</li>
 *   <li>Atomic increment to prevent duplicate numbers</li>
 *   <li>Starts at 0, increments for each new issue</li>
 *   <li>Thread-safe via database locking mechanisms</li>
 * </ul>
 * 
 * <p>Usage Example:
 * <pre>
 * ProjectIssueCounter counter = counterRepository.findByProjectIdForUpdate(projectId);
 * Integer nextNumber = counter.incrementAndGet();
 * String issueKey = project.getKey() + "-" + nextNumber; // e.g., "PROJ-1"
 * </pre>
 * 
 * <p>Important Notes:
 * <ul>
 *   <li>Use pessimistic locking when incrementing to ensure atomicity</li>
 *   <li>Counter is created automatically when project is created</li>
 *   <li>Counter is deleted when project is deleted (cascade)</li>
 *   <li>Cannot be reset once issues exist (business rule)</li>
 * </ul>
 * 
 * @see Project
 * @since 1.0.0
 */
@Entity
@Table(name = "project_issue_counters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectIssueCounter {

    /**
     * Project ID (serves as primary key).
     * Uses the same ID as the associated project.
     * This creates a one-to-one relationship with shared primary key.
     */
    @Id
    @Column(name = "project_id", nullable = false, columnDefinition = "uuid")
    private UUID projectId;

    /**
     * The project this counter belongs to.
     * One-to-one relationship with shared primary key.
     * The projectId is derived from this relationship.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId // Uses project.id as the primary key
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;

    /**
     * The last sequential number used for issue generation.
     * Starts at 0 when project is created.
     * Incremented atomically for each new issue.
     * 
     * <p>Example sequence:
     * <ul>
     *   <li>Initial value: 0</li>
     *   <li>First issue created: lastNumber becomes 1 (PROJ-1)</li>
     *   <li>Second issue created: lastNumber becomes 2 (PROJ-2)</li>
     *   <li>And so on...</li>
     * </ul>
     */
    @Column(name = "last_number", nullable = false)
    @Builder.Default
    private Integer lastNumber = 0;

    // ========== Business Logic Methods ==========

    /**
     * Increments the counter and returns the new value.
     * This is the primary method for generating new issue numbers.
     * 
     * <p><b>IMPORTANT:</b> This method only increments the in-memory value.
     * You MUST save the entity via repository to persist the change.
     * Use pessimistic locking when retrieving this entity to ensure atomicity.
     * 
     * <p>Thread-safe usage pattern:
     * <pre>
     * {@code
     * // Repository method with locking:
     * @Lock(LockModeType.PESSIMISTIC_WRITE)
     * @Query("SELECT pic FROM ProjectIssueCounter pic WHERE pic.projectId = :projectId")
     * Optional<ProjectIssueCounter> findByProjectIdForUpdate(@Param("projectId") UUID projectId);
     * 
     * // Service usage:
     * ProjectIssueCounter counter = counterRepository.findByProjectIdForUpdate(projectId).get();
     * Integer nextNumber = counter.incrementAndGet();
     * counterRepository.save(counter); // Persist the increment
     * }
     * </pre>
     * 
     * @return The new counter value (incremented by 1)
     */
    public Integer incrementAndGet() {
        this.lastNumber++;
        return this.lastNumber;
    }

    /**
     * Gets the next issue number without incrementing.
     * Useful for previewing what the next issue number will be.
     * 
     * @return The next issue number (lastNumber + 1)
     */
    public Integer getNextNumber() {
        return this.lastNumber + 1;
    }

    /**
     * Checks if any issues have been created in this project.
     * 
     * @return true if lastNumber > 0 (issues exist)
     */
    public boolean hasIssues() {
        return this.lastNumber > 0;
    }

    /**
     * Gets the count of issues created in this project.
     * Note: This assumes issues are numbered sequentially without gaps.
     * If issues can be deleted, this count may not match the actual issue count.
     * 
     * @return Number of issues created (same as lastNumber)
     */
    public Integer getIssueCount() {
        return this.lastNumber;
    }

    /**
     * Resets the counter to zero.
     * <b>WARNING:</b> This should only be used in special circumstances
     * (e.g., clearing a test project). Resetting a counter with existing
     * issues will cause duplicate issue keys.
     * 
     * <p>Business Rule: Do NOT reset counter if issues exist.
     * 
     * @throws IllegalStateException if counter has been used (lastNumber > 0)
     */
    public void reset() {
        if (this.lastNumber > 0) {
            throw new IllegalStateException(
                "Cannot reset counter: Issues already exist. " +
                "Last number: " + this.lastNumber
            );
        }
        this.lastNumber = 0;
    }

    /**
     * Validates that the counter state is consistent.
     * 
     * @return true if counter is valid
     * @throws IllegalStateException if validation fails
     */
    public boolean validate() {
        if (this.lastNumber == null) {
            throw new IllegalStateException("Last number cannot be null");
        }
        if (this.lastNumber < 0) {
            throw new IllegalStateException("Last number cannot be negative: " + this.lastNumber);
        }
        return true;
    }

    /**
     * Sets the last number directly.
     * <b>WARNING:</b> Only use this for data migration or special cases.
     * Normal issue creation should use incrementAndGet().
     * 
     * @param lastNumber New counter value
     * @throws IllegalArgumentException if value is negative
     */
    public void setLastNumber(Integer lastNumber) {
        if (lastNumber != null && lastNumber < 0) {
            throw new IllegalArgumentException("Last number cannot be negative: " + lastNumber);
        }
        this.lastNumber = lastNumber != null ? lastNumber : 0;
    }

    // ========== Convenience Methods ==========

    /**
     * Gets the project ID without loading the full project entity.
     * 
     * @return Project UUID
     */
    public UUID getProjectId() {
        return this.projectId;
    }

    /**
     * Gets the project key without loading the full project.
     * This will cause lazy loading if project is not already loaded.
     * 
     * @return Project key, or null if project not set
     */
    public String getProjectKey() {
        return this.project != null ? this.project.getKey() : null;
    }

    /**
     * Generates the next issue key preview.
     * Shows what the next issue key will be without incrementing.
     * 
     * @return Issue key preview (e.g., "PROJ-1")
     * @throws IllegalStateException if project is not set
     */
    public String previewNextIssueKey() {
        if (this.project == null) {
            throw new IllegalStateException("Project must be set to generate issue key");
        }
        String projectKey = this.project.getKey();
        if (projectKey == null) {
            throw new IllegalStateException("Project key must be set to generate issue key");
        }
        return projectKey + "-" + getNextNumber();
    }

    // ========== equals, hashCode, toString ==========

    /**
     * Equals based on project ID.
     * Two counters are equal if they belong to the same project.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectIssueCounter that = (ProjectIssueCounter) o;
        return projectId != null && projectId.equals(that.projectId);
    }

    /**
     * HashCode based on project ID.
     */
    @Override
    public int hashCode() {
        return Objects.hash(projectId);
    }

    /**
     * String representation showing counter details.
     */
    @Override
    public String toString() {
        return "ProjectIssueCounter{" +
                "projectId=" + projectId +
                ", lastNumber=" + lastNumber +
                ", nextNumber=" + getNextNumber() +
                '}';
    }
}