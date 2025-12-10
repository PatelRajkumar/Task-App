package com.pm.taskapp.task.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import com.pm.taskapp.auth.enitity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "issue_history", indexes = {
        @Index(name = "idx_issue_history_issue_id", columnList = "issue_id"),
        @Index(name = "idx_issue_history_changed_at", columnList = "changed_at")
})
public class IssueHistory {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Issue issue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User changedBy;

    @Column(name = "field", nullable = false, updatable = false, length = 100)
    private String field;

    @Column(name = "old_value", updatable = false)
    private String oldValue;

    @Column(name = "new_value", updatable = false)
    private String newValue;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    @PrePersist
    protected void onCreate() {
        if (this.changedAt == null) {
            this.changedAt = Instant.now();
        }
    }

    public boolean isStatusChange() {
        return "status".equals(this.field);
    }

    public boolean isAssignmentChange() {
        return "assignee".equals(this.field);
    }

    public boolean isPriorityChange() {
        return "priority".equals(this.field);
    }

    public boolean isTitleChange() {
        return "title".equals(this.field);
    }

    public boolean isFieldCreation() {
        return this.oldValue == null;
    }

    public boolean isFieldRemoval() {
        return this.newValue == null;
    }

    public String getFormattedChange() {
        if (isFieldCreation()) {
            return String.format("%s set to '%s'", field, newValue);
        } else if (isFieldRemoval()) {
            return String.format("%s removed (was '%s')", field, oldValue);
        } else {
            return String.format("%s changed from '%s' to '%s'", field, oldValue, newValue);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IssueHistory that = (IssueHistory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IssueHistory{" +
                "id=" + id +
                ", field='" + field + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }

    public static IssueHistory createChange(
            Issue issue,
            String field,
            String oldValue,
            String newValue,
            User changedBy) {
        return IssueHistory.builder()
                .issue(issue)
                .field(field)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(changedBy)
                .build();
    }

    public static IssueHistory forStatusChange(
            Issue issue,
            String oldStatus,
            String newStatus,
            User changedBy) {
        return createChange(issue, "status", oldStatus, newStatus, changedBy);
    }

    public static IssueHistory forAssignmentChange(
            Issue issue,
            String oldAssignee,
            String newAssignee,
            User changedBy) {
        return createChange(issue, "assignee", oldAssignee, newAssignee, changedBy);
    }

    public static IssueHistory forPriorityChange(
            Issue issue,
            String oldPriority,
            String newPriority,
            User changedBy) {
        return createChange(issue, "priority", oldPriority, newPriority, changedBy);
    }

    public static IssueHistory forTitleChange(
            Issue issue,
            String oldTitle,
            String newTitle,
            User changedBy) {
        return createChange(issue, "title", oldTitle, newTitle, changedBy);
    }
}
