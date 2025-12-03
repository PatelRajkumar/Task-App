package com.pm.taskapp.project.entity;

import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.project.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a project member (user-project relationship with role).
 * Associates users with projects and defines their role/permissions.
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Links users to projects with specific roles</li>
 *   <li>Enforces unique constraint (one role per user per project)</li>
 *   <li>Tracks when user joined the project</li>
 *   <li>Supports role-based access control (RBAC)</li>
 * </ul>
 * 
 * <p>Business Rules:
 * <ul>
 *   <li>Each user can have only one role per project</li>
 *   <li>Every project must have at least one OWNER (enforced by DB trigger)</li>
 *   <li>OWNER role can only be assigned via ownership transfer</li>
 *   <li>Role hierarchy: OWNER > ADMIN > MEMBER > VIEWER</li>
 * </ul>
 * 
 * @see Project
 * @see User
 * @see ProjectRole
 * @since 1.0.0
 */
@Entity
@Table(
    name = "project_members",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_project_members_project_user",
            columnNames = {"project_id", "user_id"}
        )
    },
    indexes = {
        @Index(name = "idx_project_members_project_id", columnList = "project_id"),
        @Index(name = "idx_project_members_user_id", columnList = "user_id"),
        @Index(name = "idx_project_members_user_role", columnList = "user_id, role")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember {

    /**
     * Unique identifier for the project member record.
     * Generated automatically using UUID.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    /**
     * The project this membership belongs to.
     * Many members can belong to one project.
     * Lazy loaded to avoid unnecessary joins.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;

    /**
     * The user who is a member of the project.
     * Many projects can have the same user.
     * Lazy loaded to avoid unnecessary joins.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    /**
     * The role/permission level of this member in the project.
     * Determines what actions the member can perform.
     * 
     * Roles (highest to lowest):
     * - OWNER: Full control
     * - ADMIN: Manage members and settings
     * - MEMBER: Create and edit issues
     * - VIEWER: Read-only access
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private ProjectRole role = ProjectRole.MEMBER;

    /**
     * Timestamp when the user joined the project.
     * Set automatically when member is added.
     */
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    // ========== Lifecycle Callbacks ==========

    /**
     * Called before persisting a new project member.
     * Sets the joinedAt timestamp.
     */
    @PrePersist
    protected void onCreate() {
        if (this.joinedAt == null) {
            this.joinedAt = Instant.now();
        }
    }

    // ========== Business Logic Methods ==========

    /**
     * Checks if this member is an owner.
     * 
     * @return true if role is OWNER
     */
    public boolean isOwner() {
        return this.role == ProjectRole.OWNER;
    }

    /**
     * Checks if this member is an admin.
     * 
     * @return true if role is ADMIN
     */
    public boolean isAdmin() {
        return this.role == ProjectRole.ADMIN;
    }

    /**
     * Checks if this member is owner or admin.
     * 
     * @return true if role is OWNER or ADMIN
     */
    public boolean isOwnerOrAdmin() {
        return this.role == ProjectRole.OWNER || this.role == ProjectRole.ADMIN;
    }

    /**
     * Checks if this member has a higher or equal role level than another member.
     * 
     * @param other Other project member to compare
     * @return true if this member's role level >= other member's role level
     */
    public boolean hasHigherOrEqualRole(ProjectMember other) {
        if (other == null || other.getRole() == null) {
            return true;
        }
        return this.role.hasHigherOrEqualLevel(other.getRole());
    }

    /**
     * Checks if this member can manage another member's role.
     * Based on role hierarchy and management rules.
     * 
     * @param targetMember Member whose role would be managed
     * @return true if this member can manage the target member
     */
    public boolean canManage(ProjectMember targetMember) {
        if (targetMember == null || this.role == null) {
            return false;
        }
        return this.role.canManageRole(targetMember.getRole());
    }

    /**
     * Checks if this member has permission to delete the project.
     * 
     * @return true if member has delete permission (OWNER only)
     */
    public boolean canDeleteProject() {
        return this.role != null && this.role.canDelete();
    }

    /**
     * Checks if this member has permission to manage project members.
     * 
     * @return true if member has manage members permission (OWNER, ADMIN)
     */
    public boolean canManageMembers() {
        return this.role != null && this.role.canManageMembers();
    }

    /**
     * Checks if this member has permission to edit project settings.
     * 
     * @return true if member has edit permission (OWNER, ADMIN)
     */
    public boolean canEditProject() {
        return this.role != null && this.role.canEditProject();
    }

    /**
     * Checks if this member has permission to create issues.
     * 
     * @return true if member has create issues permission (OWNER, ADMIN, MEMBER)
     */
    public boolean canCreateIssues() {
        return this.role != null && this.role.canCreateIssues();
    }

    /**
     * Updates the member's role.
     * Does not persist - must be saved via repository.
     * 
     * @param newRole New role to assign
     * @throws IllegalArgumentException if newRole is null
     */
    public void updateRole(ProjectRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.role = newRole;
    }

    /**
     * Gets the project ID without loading the full project entity.
     * Useful for avoiding lazy loading when only ID is needed.
     * 
     * @return Project UUID, or null if project not set
     */
    public UUID getProjectId() {
        return this.project != null ? this.project.getId() : null;
    }

    /**
     * Gets the user ID without loading the full user entity.
     * Useful for avoiding lazy loading when only ID is needed.
     * 
     * @return User UUID, or null if user not set
     */
    public UUID getUserId() {
        return this.user != null ? this.user.getId() : null;
    }

    /**
     * Gets the user's email without loading the full user entity.
     * Uses Hibernate's ability to access simple properties without full initialization.
     * 
     * @return User email, or null if user not set
     */
    public String getUserEmail() {
        return this.user != null ? this.user.getEmail() : null;
    }

    /**
     * Gets the user's name without loading the full user entity.
     * 
     * @return User name, or null if user not set
     */
    public String getUserName() {
        return this.user != null ? this.user.getName() : null;
    }

    // ========== equals, hashCode, toString ==========

    /**
     * Equals based on business key (project + user combination).
     * Two project members are equal if they represent the same user in the same project.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectMember that = (ProjectMember) o;
        
        // Use IDs for comparison if available
        if (this.project != null && that.project != null && 
            this.user != null && that.user != null) {
            return Objects.equals(this.project.getId(), that.project.getId()) &&
                   Objects.equals(this.user.getId(), that.user.getId());
        }
        
        // Fallback to instance equality
        return false;
    }

    /**
     * HashCode based on business key (project + user).
     * Uses project ID and user ID for consistent hashing.
     */
    @Override
    public int hashCode() {
        return Objects.hash(
            this.project != null ? this.project.getId() : null,
            this.user != null ? this.user.getId() : null
        );
    }

    /**
     * String representation showing key details.
     * Does not include full project/user objects to avoid lazy loading.
     */
    @Override
    public String toString() {
        return "ProjectMember{" +
                "id=" + id +
                ", projectId=" + getProjectId() +
                ", userId=" + getUserId() +
                ", role=" + role +
                ", joinedAt=" + joinedAt +
                '}';
    }
}