package com.pm.taskapp.project.enums;

/**
 * Enum representing project visibility levels.
 * Determines who can view and access the project.
 * 
 * <p>Visibility Rules:
 * <ul>
 *   <li><b>PUBLIC</b>: Anyone can view the project, even non-members</li>
 *   <li><b>PRIVATE</b>: Only project members can view the project</li>
 * </ul>
 * 
 * <p>Note: Archived projects are only visible to project OWNERs,
 * regardless of visibility setting.
 * 
 * @see com.pm.taskapp.project.entity.Project
 * @since 1.0.0
 */
public enum ProjectVisibility {
    
    /**
     * Public project - Anyone can view.
     * <p>Use case: Open source projects, public portfolios, community projects.
     */
    PUBLIC("Public", "Anyone can view this project"),
    
    /**
     * Private project - Only members can view.
     * <p>Use case: Internal projects, confidential work, team collaboration.
     */
    PRIVATE("Private", "Only project members can view this project");
    
    private final String displayName;
    private final String description;
    
    /**
     * Constructor for ProjectVisibility enum.
     * 
     * @param displayName Human-readable name for display in UI
     * @param description Detailed description of visibility level
     */
    ProjectVisibility(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Gets the human-readable display name.
     * 
     * @return Display name (e.g., "Public", "Private")
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the detailed description of this visibility level.
     * 
     * @return Description text
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this visibility level is public.
     * 
     * @return true if PUBLIC, false otherwise
     */
    public boolean isPublic() {
        return this == PUBLIC;
    }
    
    /**
     * Checks if this visibility level is private.
     * 
     * @return true if PRIVATE, false otherwise
     */
    public boolean isPrivate() {
        return this == PRIVATE;
    }
    
    /**
     * Gets the default visibility level for new projects.
     * 
     * @return PRIVATE (default for security)
     */
    public static ProjectVisibility getDefault() {
        return PRIVATE;
    }
    
    /**
     * Converts a string to ProjectVisibility enum.
     * Case-insensitive matching.
     * 
     * @param value String value to convert
     * @return Corresponding ProjectVisibility enum
     * @throws IllegalArgumentException if value is invalid
     */
    public static ProjectVisibility fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Visibility value cannot be null");
        }
        
        try {
            return ProjectVisibility.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid visibility value: " + value + 
                ". Valid values are: PUBLIC, PRIVATE"
            );
        }
    }
    
    @Override
    public String toString() {
        return this.name();
    }
}