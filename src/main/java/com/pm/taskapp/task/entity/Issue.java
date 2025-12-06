package com.pm.taskapp.task.entity;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import com.pm.taskapp.auth.enitity.User;
import com.pm.taskapp.project.entity.Project;
import com.pm.taskapp.task.enums.IssuePriority;
import com.pm.taskapp.task.enums.IssueStatus;
import com.pm.taskapp.task.enums.IssueType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "issues", uniqueConstraints = {
        @UniqueConstraint(name = "uk_issues_project_sequential", columnNames = { "project_id", "sequential_number" })
}, indexes = {
        @Index(name = "idx_issues_key", columnList = "key", unique = true),
        @Index(name = "idx_issues_project_id", columnList = "project_id"),
        @Index(name = "idx_issues_assignee_id", columnList = "assignee_id"),
        @Index(name = "idx_issues_reporter_id", columnList = "reporter_id"),
        @Index(name = "idx_issues_status", columnList = "status"),
        @Index(name = "idx_issues_type", columnList = "type"),
        @Index(name = "idx_issues_priority", columnList = "priority"),
        @Index(name = "idx_issues_created_at", columnList = "created_at"),
        @Index(name = "idx_issues_deleted", columnList = "is_deleted")
})
public class Issue {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "key", unique = true, nullable = false, length = 50, updatable = false)
    private String key;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;

    @Column(name = "sequential_number", nullable = false, updatable = false)
    private Integer sequentialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private IssueType type = IssueType.TASK;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private IssuePriority priority = IssuePriority.getDefault();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private IssueStatus status = IssueStatus.TODO;

    @JoinColumn(name = "reporter_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User reporter;

    @JoinColumn(name = "assignee_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User assignee;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User deletedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ========== Lifecycle Callbacks ==========

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public boolean isOpen() {
        return this.status != null && !this.status.isFinal();
    }

    public boolean isResolved() {
        return this.status != null && this.status.isFinal();
    }

    public boolean isOverdue() {
        if (this.dueDate == null || !isOpen()) {
            return false;
        }
        return this.dueDate.isBefore(LocalDate.now());
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDeleted);
    }

    public boolean isAssigned() {
        return this.assignee != null;
    }

    public boolean isReporter(UUID userId) {
        return this.reporter != null &&
                this.reporter.getId() != null &&
                this.reporter.getId().equals(userId);
    }

    public boolean isAssignee(UUID userId) {
        return this.assignee != null &&
                this.assignee.getId() != null &&
                this.assignee.getId().equals(userId);
    }

    // ========== equals, hashCode, toString ==========

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Issue issue = (Issue) o;
        return key != null && key.equals(issue.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "Issue{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", priority=" + priority +
                ", status=" + status +
                ", isDeleted=" + isDeleted +
                ", createdAt=" + createdAt +
                '}';
    }

}
