package com.pm.taskapp.attachment.enums;

import java.util.Arrays;
import java.util.Set;

/**
 * Enum for file categories.
 * Categorizes attachments by their type/content.
 * 
 * <p>Categories:
 * <ul>
 *   <li>IMAGE: Image files (PNG, JPEG, GIF, etc.)</li>
 *   <li>DOCUMENT: Document files (PDF, DOCX, etc.)</li>
 *   <li>SPREADSHEET: Spreadsheet files (XLSX, CSV, etc.)</li>
 *   <li>ARCHIVE: Compressed files (ZIP, TAR, etc.)</li>
 *   <li>OTHER: Other file types</li>
 * </ul>
 *
 * <p>Usage:
 * Determined by MIME type during file upload. Used for filtering
 * and organizing attachments by category.
 *
 * <p>Note: This enum is informational and not enforced at database level.
 * Can be extended for future features like preview generation or
 * category-based access control.
 *
 * @since 1.0.0
 */
public enum FileCategory {

    IMAGE(
            "Image",
            "Image files (PNG, JPEG, GIF, BMP, WEBP, SVG)",
            Set.of("image/png", "image/jpeg", "image/gif", "image/bmp", 
                   "image/webp", "image/svg+xml")),

    DOCUMENT(
            "Document",
            "Document files (PDF, DOCX, DOC, TXT)",
            Set.of("application/pdf", "application/msword",
                   "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                   "text/plain")),

    SPREADSHEET(
            "Spreadsheet",
            "Spreadsheet files (XLSX, XLS, CSV)",
            Set.of("application/vnd.ms-excel",
                   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                   "text/csv")),

    ARCHIVE(
            "Archive",
            "Compressed/Archive files (ZIP, RAR, 7Z, TAR, GZ)",
            Set.of("application/zip", "application/x-rar-compressed",
                   "application/x-7z-compressed", "application/x-tar",
                   "application/gzip", "application/x-gzip")),

    OTHER(
            "Other",
            "Other file types",
            Set.of()); // Matches any MIME type not in above categories

    private final String displayName;
    private final String description;
    private final Set<String> mimeTypes;

    FileCategory(String displayName, String description, Set<String> mimeTypes) {
        this.displayName = displayName;
        this.description = description;
        this.mimeTypes = mimeTypes;
    }

    /**
     * Gets the display name for UI.
     * 
     * @return human-readable category name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the description of this category.
     * 
     * @return category description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the set of MIME types for this category.
     * 
     * @return set of MIME types (immutable)
     */
    public Set<String> getMimeTypes() {
        return Set.copyOf(mimeTypes);
    }

    /**
     * Determines file category from MIME type.
     * 
     * <p>Examples:
     * <ul>
     *   <li>"image/png" → IMAGE</li>
     *   <li>"application/pdf" → DOCUMENT</li>
     *   <li>"text/csv" → SPREADSHEET</li>
     *   <li>"application/zip" → ARCHIVE</li>
     *   <li>"unknown/type" → OTHER</li>
     * </ul>
     * 
     * @param mimeType MIME type string
     * @return FileCategory based on MIME type, or OTHER if not recognized
     */
    public static FileCategory fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return OTHER;
        }

        String cleanMimeType = mimeType.toLowerCase().trim();

        for (FileCategory category : FileCategory.values()) {
            if (category != OTHER && category.mimeTypes.contains(cleanMimeType)) {
                return category;
            }
        }

        return OTHER;
    }

    /**
     * Checks if this is an image category.
     * 
     * @return true if category is IMAGE
     */
    public boolean isImage() {
        return this == IMAGE;
    }

    /**
     * Checks if this is a document category.
     * 
     * @return true if category is DOCUMENT
     */
    public boolean isDocument() {
        return this == DOCUMENT;
    }

    /**
     * Checks if this is a spreadsheet category.
     * 
     * @return true if category is SPREADSHEET
     */
    public boolean isSpreadsheet() {
        return this == SPREADSHEET;
    }

    /**
     * Checks if this is an archive category.
     * 
     * @return true if category is ARCHIVE
     */
    public boolean isArchive() {
        return this == ARCHIVE;
    }

    /**
     * Checks if this is the "other" category.
     * 
     * @return true if category is OTHER
     */
    public boolean isOther() {
        return this == OTHER;
    }

    /**
     * Parse file category from string (case-insensitive).
     * 
     * @param category Category string
     * @return FileCategory enum value
     * @throws IllegalArgumentException if category is invalid
     */
    public static FileCategory fromString(String category) {
        if (category == null || category.trim().isEmpty()) {
            return OTHER;
        }

        try {
            return FileCategory.valueOf(category.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            // Invalid category defaults to OTHER instead of throwing
            return OTHER;
        }
    }

    @Override
    public String toString() {
        return this.name();
    }
}