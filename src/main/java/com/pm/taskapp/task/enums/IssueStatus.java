package com.pm.taskapp.task.enums;

import java.util.Arrays;
import java.util.Set;

public enum IssueStatus {

    TODO(
            "Todo",
            "Task/issue is not started yet"),

    INPROGRESS(
            "In Progress",
            "Task/issue is in progress"),

    DONE(
            "Done",
            "Task/issue is completed");

    private final String displayName;
    private final String description;

    IssueStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return this.name();
    }

    /**
     * Checks if transition from current status to target status is valid.
     * Enforces strict linear workflow: TODO → INPROGRESS → DONE → TODO
     * 
     * @param targetStatus The status to transition to
     * @return true if transition is valid
     */
    public boolean canTransitionTo(IssueStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }

        // Allow staying in same status (no-op)
        if (this == targetStatus) {
            return true;
        }

        return switch (this) {
            case TODO -> targetStatus == INPROGRESS;
            case INPROGRESS -> targetStatus == DONE;
            case DONE -> targetStatus == TODO;
        };
    }

    /**
     * Gets all valid statuses that this status can transition to.
     * 
     * @return Set of valid next statuses
     */
    public Set<IssueStatus> getAllowedTransitions() {
        return switch (this) {
            case TODO -> Set.of(INPROGRESS);
            case INPROGRESS -> Set.of(DONE);
            case DONE -> Set.of(TODO);
        };
    }

    /**
     * Validates if transition is allowed and throws exception if not.
     * 
     * @param targetStatus The status to transition to
     * @throws IllegalArgumentException if transition is invalid
     */
    public void validateTransition(IssueStatus targetStatus) {
        if (!canTransitionTo(targetStatus)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid status transition from %s to %s. Allowed transitions: %s",
                            this.name(),
                            targetStatus.name(),
                            getAllowedTransitions().stream()
                                    .map(IssueStatus::name)
                                    .toList()));
        }
    }

    // Helper Methods

    /**
     * Checks if this is the initial status.
     * 
     * @return true if status is TODO
     */
    public boolean isInitial() {
        return this == TODO;
    }

    /**
     * Checks if this is the final status.
     * 
     * @return true if status is DONE
     */
    public boolean isFinal() {
        return this == DONE;
    }

    /**
     * Checks if issue is in progress.
     * 
     * @return true if status is INPROGRESS
     */
    public boolean isInProgress() {
        return this == INPROGRESS;
    }

    /**
     * Parse status from string (case-insensitive).
     * 
     * @param status Status string
     * @return IssueStatus enum value
     * @throws IllegalArgumentException if status is invalid
     */
    public static IssueStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        try {
            return IssueStatus.valueOf(status.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid status: '%s'. Valid values are: %s",
                            status,
                            Arrays.toString(IssueStatus.values())));
        }
    }
}
