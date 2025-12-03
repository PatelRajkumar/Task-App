package com.pm.taskapp.project.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enum representing project member roles with hierarchical permissions.
 * Each role has a specific level and set of permissions within a project.
 * 
 * <p>Role Hierarchy (highest to lowest):
 * <ol>
 *   <li><b>OWNER</b> (Level 4): Full control, can delete/transfer project</li>
 *   <li><b>ADMIN</b> (Level 3): Manage members and settings</li>
 *   <li><b>MEMBER</b> (Level 2): Create and edit issues</li>
 *   <li><b>VIEWER</b> (Level 1): Read-only access</li>
 * </ol>
 * 
 * <p>Permission Rules:
 * <ul>
 *   <li>Higher roles inherit all permissions of lower roles</li>
 *   <li>Every project must have at least one OWNER</li>
 *   <li>OWNER role can only be assigned via ownership transfer</li>
 * </ul>
 * 
 * @see com.pm.taskapp.project.entity.ProjectMember
 * @since 1.0.0
 */
public enum ProjectRole {
    
    /**
     * Owner role - Full project control.
     * <p>Permissions:
     * <ul>
     *   <li>All ADMIN permissions</li>
     *   <li>Delete/archive project</li>
     *   <li>Transfer ownership to another member</li>
     *   <li>View archived projects</li>
     *   <li>Permanently delete project</li>
     *   <li>Cannot be directly assigned (must transfer ownership)</li>
     * </ul>
     * <p><b>Note:</b> Every project must have at least one OWNER.
     * Protected by database trigger.
     */
    OWNER(
        "Owner", 
        "Full control over the project including deletion and ownership transfer",
        4,
        true,  // canDelete
        true,  // canManageMembers
        true,  // canEditProject
        true,  // canCreateIssues
        true   // canViewProject
    ),
    
    /**
     * Admin role - Project management without ownership.
     * <p>Permissions:
     * <ul>
     *   <li>All MEMBER permissions</li>
     *   <li>Add/remove MEMBER and VIEWER roles</li>
     *   <li>Update member roles (except OWNER and other ADMINs)</li>
     *   <li>Update project settings (name, description, visibility)</li>
     *   <li>Cannot delete project</li>
     *   <li>Cannot manage OWNER or other ADMIN members</li>
     * </ul>
     */
    ADMIN(
        "Admin",
        "Manage project members and settings",
        3,
        false, // canDelete
        true,  // canManageMembers (limited)
        true,  // canEditProject
        true,  // canCreateIssues
        true   // canViewProject
    ),
    
    /**
     * Member role - Standard project contributor.
     * <p>Permissions:
     * <ul>
     *   <li>All VIEWER permissions</li>
     *   <li>Create new issues</li>
     *   <li>Edit own issues</li>
     *   <li>Edit issues assigned to them</li>
     *   <li>Add comments</li>
     *   <li>Upload attachments</li>
     *   <li>Cannot manage members or project settings</li>
     * </ul>
     */
    MEMBER(
        "Member",
        "Create and edit issues within the project",
        2,
        false, // canDelete
        false, // canManageMembers
        false, // canEditProject
        true,  // canCreateIssues
        true   // canViewProject
    ),
    
    /**
     * Viewer role - Read-only access.
     * <p>Permissions:
     * <ul>
     *   <li>View project details</li>
     *   <li>View all issues</li>
     *   <li>View comments</li>
     *   <li>View attachments</li>
     *   <li>Cannot create, edit, or delete anything</li>
     * </ul>
     */
    VIEWER(
        "Viewer",
        "Read-only access to the project",
        1,
        false, // canDelete
        false, // canManageMembers
        false, // canEditProject
        false, // canCreateIssues
        true   // canViewProject
    );
    
    private final String displayName;
    private final String description;
    private final int level;
    private final boolean canDelete;
    private final boolean canManageMembers;
    private final boolean canEditProject;
    private final boolean canCreateIssues;
    private final boolean canViewProject;
    
    /**
     * Constructor for ProjectRole enum.
     * 
     * @param displayName Human-readable name for UI display
     * @param description Detailed description of role capabilities
     * @param level Hierarchical level (1-4, higher = more permissions)
     * @param canDelete Permission to delete/archive project
     * @param canManageMembers Permission to add/remove/update members
     * @param canEditProject Permission to update project settings
     * @param canCreateIssues Permission to create new issues
     * @param canViewProject Permission to view project
     */
    ProjectRole(String displayName, String description, int level,
                boolean canDelete, boolean canManageMembers, 
                boolean canEditProject, boolean canCreateIssues, 
                boolean canViewProject) {
        this.displayName = displayName;
        this.description = description;
        this.level = level;
        this.canDelete = canDelete;
        this.canManageMembers = canManageMembers;
        this.canEditProject = canEditProject;
        this.canCreateIssues = canCreateIssues;
        this.canViewProject = canViewProject;
    }
    
    // Getters
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getLevel() {
        return level;
    }
    
    public boolean canDelete() {
        return canDelete;
    }
    
    public boolean canManageMembers() {
        return canManageMembers;
    }
    
    public boolean canEditProject() {
        return canEditProject;
    }
    
    public boolean canCreateIssues() {
        return canCreateIssues;
    }
    
    public boolean canViewProject() {
        return canViewProject;
    }
    
    // Role Comparison Methods
    
    /**
     * Checks if this role has a higher or equal level than another role.
     * 
     * @param other Role to compare with
     * @return true if this role's level >= other role's level
     */
    public boolean hasHigherOrEqualLevel(ProjectRole other) {
        return this.level >= other.level;
    }
    
    /**
     * Checks if this role has a higher level than another role.
     * 
     * @param other Role to compare with
     * @return true if this role's level > other role's level
     */
    public boolean hasHigherLevel(ProjectRole other) {
        return this.level > other.level;
    }
    
    /**
     * Checks if this role has a lower level than another role.
     * 
     * @param other Role to compare with
     * @return true if this role's level < other role's level
     */
    public boolean hasLowerLevel(ProjectRole other) {
        return this.level < other.level;
    }
    
    // Permission Check Methods
    
    /**
     * Checks if this role can manage the target role.
     * Rules:
     * - OWNER can manage all roles
     * - ADMIN can manage MEMBER and VIEWER only
     * - MEMBER and VIEWER cannot manage anyone
     * 
     * @param targetRole Role to be managed
     * @return true if this role can manage the target role
     */
    public boolean canManageRole(ProjectRole targetRole) {
        if (this == OWNER) {
            return true; // OWNER can manage all roles
        }
        if (this == ADMIN) {
            return targetRole == MEMBER || targetRole == VIEWER;
        }
        return false; // MEMBER and VIEWER cannot manage
    }
    
    /**
     * Checks if this role is an owner.
     * 
     * @return true if OWNER role
     */
    public boolean isOwner() {
        return this == OWNER;
    }
    
    /**
     * Checks if this role is owner or admin.
     * 
     * @return true if OWNER or ADMIN
     */
    public boolean isOwnerOrAdmin() {
        return this == OWNER || this == ADMIN;
    }
    
    /**
     * Checks if this role can perform actions (not just view).
     * 
     * @return true if can create/edit (MEMBER or higher)
     */
    public boolean canPerformActions() {
        return this.level >= MEMBER.level;
    }
    
    // Static Utility Methods
    
    /**
     * Gets the default role for new project members.
     * 
     * @return MEMBER (balanced permissions for collaboration)
     */
    public static ProjectRole getDefault() {
        return MEMBER;
    }
    
    /**
     * Gets roles that can be directly assigned when adding members.
     * OWNER is excluded as it can only be assigned via ownership transfer.
     * 
     * @return Set of assignable roles (ADMIN, MEMBER, VIEWER)
     */
    public static Set<ProjectRole> getAssignableRoles() {
        return Arrays.stream(ProjectRole.values())
                .filter(role -> role != OWNER)
                .collect(Collectors.toSet());
    }
    
    /**
     * Gets all roles that an ADMIN can manage.
     * 
     * @return List of roles (MEMBER, VIEWER)
     */
    public static List<ProjectRole> getAdminManageableRoles() {
        return Arrays.asList(MEMBER, VIEWER);
    }
    
    /**
     * Gets all roles in hierarchical order (highest to lowest).
     * 
     * @return List of roles sorted by level descending
     */
    public static List<ProjectRole> getRolesInHierarchicalOrder() {
        return Arrays.stream(ProjectRole.values())
                .sorted((r1, r2) -> Integer.compare(r2.level, r1.level))
                .collect(Collectors.toList());
    }
    
    /**
     * Converts a string to ProjectRole enum.
     * Case-insensitive matching.
     * 
     * @param value String value to convert
     * @return Corresponding ProjectRole enum
     * @throws IllegalArgumentException if value is invalid
     */
    public static ProjectRole fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Role value cannot be null");
        }
        
        try {
            return ProjectRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid role value: " + value + 
                ". Valid values are: OWNER, ADMIN, MEMBER, VIEWER"
            );
        }
    }
    
    /**
     * Validates if a role can be directly assigned.
     * OWNER cannot be directly assigned (must use ownership transfer).
     * 
     * @param role Role to validate
     * @return true if role can be directly assigned
     */
    public static boolean isAssignable(ProjectRole role) {
        return role != OWNER;
    }
    
    /**
     * Gets a description of the role hierarchy.
     * Useful for documentation or help text.
     * 
     * @return String describing the role hierarchy
     */
    public static String getRoleHierarchyDescription() {
        return "Role Hierarchy (highest to lowest):\n" +
               "1. OWNER - Full control, can delete project and transfer ownership\n" +
               "2. ADMIN - Manage members and settings, cannot delete project\n" +
               "3. MEMBER - Create and edit issues, cannot manage members\n" +
               "4. VIEWER - Read-only access, cannot create or edit";
    }
    
    @Override
    public String toString() {
        return this.name();
    }
}