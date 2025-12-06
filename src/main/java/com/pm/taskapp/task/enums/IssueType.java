package com.pm.taskapp.task.enums;

import java.util.Arrays;

public enum IssueType {
    BUG("Bug", "This is for bug"),
    TASK("Task", "This is for task");

    private final String displayName;
    private final String description;

    IssueType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Parse issue type from string (case-insensitive).
     * 
     * @param type Type string (e.g., "task", "TASK", "bug", "BUG")
     * @return IssueType enum value
     * @throws IllegalArgumentException if type is invalid
     */
    public static IssueType fromString(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue type cannot be null or empty");
        }

        try {
            return IssueType.valueOf(type.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid issue type: '%s'. Valid values are: %s",
                            type,
                            Arrays.toString(IssueType.values())));
        }
    }

    /**
     * Checks if this is a bug type.
     * 
     * @return true if type is BUG
     */
    public boolean isBug() {
        return this == BUG;
    }

    /**
     * Checks if this is a task type.
     * 
     * @return true if type is TASK
     */
    public boolean isTask() {
        return this == TASK;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
