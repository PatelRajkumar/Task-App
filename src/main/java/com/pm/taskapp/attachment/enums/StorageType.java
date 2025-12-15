package com.pm.taskapp.attachment.enums;

import java.util.Arrays;

/**
 * Enum for attachment storage types.
 * Defines where files can be stored (cloud or local).
 *
 * <p>
 * Storage Types:
 * <ul>
 * <li>S3: Amazon S3 object storage (production)</li>
 * <li>LOCAL: Local file system (development/fallback)</li>
 * </ul>
 *
 * <p>
 * Database Constraint:
 * 
 * <pre>
 * CONSTRAINT chk_storage_type CHECK (storage_type IN ('S3', 'LOCAL'))
 * </pre>
 *
 * @since 1.0.0
 */
public enum StorageType {

    S3(
            "Amazon S3",
            "Files stored in AWS S3 bucket"),

    LOCAL(
            "Local Storage",
            "Files stored in local file system (dev/fallback)");

    private final String displayName;
    private final String description;

    StorageType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the display name for UI.
     * 
     * @return human-readable storage type name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the description of this storage type.
     * 
     * @return storage type description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this is S3 storage.
     * 
     * @return true if storage type is S3
     */
    public boolean isS3() {
        return this == S3;
    }

    /**
     * Checks if this is local storage.
     * 
     * @return true if storage type is LOCAL
     */
    public boolean isLocal() {
        return this == LOCAL;
    }

    /**
     * Parse storage type from string (case-insensitive).
     * Used for converting database values or user input to enum.
     * 
     * <p>
     * Examples:
     * <ul>
     * <li>"s3" → S3</li>
     * <li>"S3" → S3</li>
     * <li>"local" → LOCAL</li>
     * <li>"LOCAL" → LOCAL</li>
     * </ul>
     * 
     * @param type Type string (e.g., "s3", "S3", "local", "LOCAL")
     * @return StorageType enum value
     * @throws IllegalArgumentException if type is invalid
     */
    public static StorageType fromString(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Storage type cannot be null or empty");
        }

        try {
            return StorageType.valueOf(type.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid storage type: '%s'. Valid values are: %s",
                            type,
                            Arrays.toString(StorageType.values())));
        }
    }

    /**
     * Gets the default storage type.
     * Returns S3 for production, but can be overridden in config.
     * 
     * @return default storage type (S3)
     */
    public static StorageType getDefault() {
        return S3;
    }

    @Override
    public String toString() {
        return this.name();
    }
}