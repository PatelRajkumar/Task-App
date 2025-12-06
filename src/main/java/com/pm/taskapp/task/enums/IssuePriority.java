package com.pm.taskapp.task.enums;

import java.util.Arrays;

public enum IssuePriority {
    LOW("Low", "This is for low priority task/bug", 1),
    MEDIUM("Medium", "This is for medium priority task/bug", 2),
    HIGH("High", "This is for high priority task/bug", 3);

    private final String displayName;
    private final String description;
    private final int level;

    IssuePriority(String displayName, String description, int level) {
        this.displayName = displayName;
        this.description = description;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHigherThan(IssuePriority other) {
        return this.level > other.level;
    }

    public boolean isLowerThan(IssuePriority other) {
        return this.level < other.level;
    }

    public boolean isHigherOrEqualTo(IssuePriority other) {
        return this.level >= other.level;
    }

    public boolean isLow() {
        return this == LOW;
    }

    public boolean isMedium() {
        return this == MEDIUM;
    }

    public boolean isHigh() {
        return this == HIGH;
    }

    public static IssuePriority getDefault() {
        return MEDIUM;
    }

    public static IssuePriority fromString(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue priority cannot be null or empty");
        }

        try {
            return IssuePriority.valueOf(type.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid priority type: '%s'. Valid values are: %s",
                            type,
                            Arrays.toString(IssuePriority.values())));
        }
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

}
