package com.pm.taskapp.project.entity;

import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.project.enums.ProjectVisibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Entity representing a project in the system.
 * Projects are the primary organizational unit containing issues, workflows, and team members.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Unique key for identification (e.g., PROJ-1, PROJ-2)</li>
 *   <li>Visibility control (PUBLIC or PRIVATE)</li>
 *   <li>Soft delete via archive flag</li>
 *   <li>Automatic timestamp management</li>
 *   <li>Relationship with members and issue counter</li>
 * </ul>
 * 
 * <p>Business Rules:
 * <ul>
 *   <li>Project key is unique and immutable after creation</li>
 *   <li>Every project must have at least one OWNER</li>
 *   <li>Creator automatically becomes first OWNER</li>
 *   <li>Archived projects only visible to OWNERs</li>
 * </ul>
 * 
 * @see ProjectMember
 * @see ProjectIssueCounter
 * @since 1.0.0
 */
@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_projects_key", columnList = "key"),
    @Index(name = "idx_projects_created_by", columnList = "created_by"),
    @Index(name = "idx_projects_archived_owner", columnList = "is_archived, created_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    /**
     * Unique identifier for the project.
     * Generated automatically using UUID.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    /**
     * Unique project key for human-readable identification.
     * Format: PROJ-{number} (e.g., PROJ-1, PROJ-2)
     * Generated automatically, immutable after creation.
     */
    @Column(name = "key", unique = true, nullable = false, length = 10, updatable = false)
    private String key;

    /**
     * Human-readable project name.
     * Required, 3-255 characters.
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Detailed project description.
     * Optional, supports markdown formatting.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Project visibility level.
     * Determines who can view the project.
     * - PUBLIC: Anyone can view
     * - PRIVATE: Only members can view
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    @Builder.Default
    private ProjectVisibility visibility = ProjectVisibility.PRIVATE;

    /**
     * User who created the project.
     * Automatically becomes the first OWNER.
     * Lazy loaded to avoid unnecessary joins.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdBy;

    /**
     * Soft delete flag.
     * When true, project is archived and only visible to OWNERs.
     * Allows restoration without losing data.
     */
    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private Boolean isArchived = false;

    /**
     * Timestamp when the project was created.
     * Set automatically on persist.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp when the project was archived.
     * Null if project is not archived.
     */
    @Column(name = "archived_at")
    private Instant archivedAt;

    /**
     * Timestamp when the project was last updated.
     * Updated automatically on any modification.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Set of all members belonging to this project.
     * Bidirectional relationship with ProjectMember.
     * Cascade operations for automatic member management.
     * Orphan removal ensures deleted members are cleaned up.
     */
    @OneToMany(
        mappedBy = "project", 
        cascade = CascadeType.ALL, 
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ProjectMember> members = new HashSet<>();

    /**
     * Issue counter for this project.
     * Used to generate sequential issue numbers (PROJ-1, PROJ-2, etc.)
     * One-to-one relationship with ProjectIssueCounter.
     */
    @OneToOne(
        mappedBy = "project", 
        cascade = CascadeType.ALL, 
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProjectIssueCounter issueCounter;

    // ========== Lifecycle Callbacks ==========

    /**
     * Called before persisting a new project.
     * Sets creation and update timestamps.
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Called before updating an existing project.
     * Updates the updatedAt timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // ========== Business Logic Methods ==========

    /**
     * Archives the project.
     * Sets isArchived flag and records archive timestamp.
     */
    public void archive() {
        this.isArchived = true;
        this.archivedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Restores an archived project.
     * Clears isArchived flag and archive timestamp.
     */
    public void restore() {
        this.isArchived = false;
        this.archivedAt = null;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if the project is active (not archived).
     * 
     * @return true if project is not archived
     */
    public boolean isActive() {
        return !this.isArchived;
    }

    /**
     * Checks if the project is public.
     * 
     * @return true if visibility is PUBLIC
     */
    public boolean isPublic() {
        return this.visibility != null && this.visibility.isPublic();
    }

    /**
     * Checks if the project is private.
     * 
     * @return true if visibility is PRIVATE
     */
    public boolean isPrivate() {
        return this.visibility != null && this.visibility.isPrivate();
    }

    /**
     * Adds a member to the project.
     * Helper method for bidirectional relationship management.
     * 
     * @param member ProjectMember to add
     */
    public void addMember(ProjectMember member) {
        if (member != null) {
            this.members.add(member);
            member.setProject(this);
        }
    }

    /**
     * Removes a member from the project.
     * Helper method for bidirectional relationship management.
     * 
     * @param member ProjectMember to remove
     */
    public void removeMember(ProjectMember member) {
        if (member != null) {
            this.members.remove(member);
            member.setProject(null);
        }
    }

    /**
     * Gets the count of members in the project.
     * 
     * @return Number of members
     */
    public int getMemberCount() {
        return this.members != null ? this.members.size() : 0;
    }

    /**
     * Checks if a user is a member of this project.
     * 
     * @param userId User ID to check
     * @return true if user is a member
     */
    public boolean hasMember(UUID userId) {
        if (userId == null || this.members == null) {
            return false;
        }
        return this.members.stream()
                .anyMatch(member -> member.getUser() != null && 
                                   userId.equals(member.getUser().getId()));
    }

    // ========== equals, hashCode, toString ==========

    /**
     * Equals based on business key (project key).
     * Two projects are equal if they have the same key.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return key != null && key.equals(project.key);
    }

    /**
     * HashCode based on business key (project key).
     * Uses key for consistent hashing.
     */
    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    /**
     * String representation showing key details.
     * Does not include collections to avoid lazy loading issues.
     */
    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", visibility=" + visibility +
                ", isArchived=" + isArchived +
                ", createdAt=" + createdAt +
                '}';
    }
}