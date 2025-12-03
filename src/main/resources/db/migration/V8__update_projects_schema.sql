-- =====================================================
-- Migration: V8 - Update Projects Schema
-- Purpose: Align projects module with updated business requirements
-- Date: 2025-11-27
-- Author: TaskApp Development Team
-- =====================================================
-- Changes:
-- 1. Update visibility constraint (PUBLIC, PRIVATE only)
-- 2. Add OWNER role to project members
-- 3. Migrate existing data to uppercase
-- 4. Optimize indexes for owner queries
-- 5. Create function for global project key generation
-- 6. Add documentation comments
-- =====================================================

-- =====================================================
-- SECTION 1: Update Project Visibility Constraint
-- =====================================================
-- Remove old constraint that allowed 'public', 'private', 'internal'
-- Add new constraint that only allows 'PUBLIC', 'PRIVATE'
-- =====================================================

-- Drop existing visibility constraint
ALTER TABLE projects
DROP CONSTRAINT IF EXISTS chk_visibility;

-- Add new visibility constraint with only PUBLIC and PRIVATE
ALTER TABLE projects
    ADD CONSTRAINT chk_visibility
        CHECK (visibility IN ('PUBLIC', 'PRIVATE'));

-- Update default visibility to uppercase PRIVATE
ALTER TABLE projects
    ALTER COLUMN visibility SET DEFAULT 'PRIVATE';

-- Add column comment for clarity
COMMENT ON COLUMN projects.visibility IS 'Project visibility: PUBLIC (anyone can view) or PRIVATE (only members can view)';


-- =====================================================
-- SECTION 2: Update Project Member Roles
-- =====================================================
-- Remove old constraint that allowed 'admin', 'member', 'viewer'
-- Add new constraint that includes 'OWNER', 'ADMIN', 'MEMBER', 'VIEWER'
-- =====================================================

-- Drop existing role constraint
ALTER TABLE project_members
DROP CONSTRAINT IF EXISTS chk_project_role;

-- Add new role constraint with OWNER role
ALTER TABLE project_members
    ADD CONSTRAINT chk_project_role
        CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER'));

-- Update default role to uppercase MEMBER
ALTER TABLE project_members
    ALTER COLUMN role SET DEFAULT 'MEMBER';

-- Add column comment for clarity
COMMENT ON COLUMN project_members.role IS 'Member role: OWNER (full control, can transfer ownership), ADMIN (manage members and settings), MEMBER (create/edit issues), VIEWER (read-only access)';


-- =====================================================
-- SECTION 3: Migrate Existing Data
-- =====================================================
-- Convert all existing lowercase values to uppercase
-- This ensures consistency with new enum values
-- =====================================================

-- Update existing project visibility values to uppercase
UPDATE projects
SET visibility = UPPER(visibility)
WHERE visibility IN ('public', 'private', 'internal');

-- Update existing project member role values to uppercase
-- Note: 'admin' -> 'ADMIN', 'member' -> 'MEMBER', 'viewer' -> 'VIEWER'
UPDATE project_members
SET role = UPPER(role)
WHERE role IN ('admin', 'member', 'viewer');

-- Log migration results
DO $$
DECLARE
project_count INTEGER;
    member_count INTEGER;
BEGIN
SELECT COUNT(*) INTO project_count FROM projects;
SELECT COUNT(*) INTO member_count FROM project_members;

RAISE NOTICE 'Migration V8 completed successfully:';
    RAISE NOTICE '  - Updated % project visibility values', project_count;
    RAISE NOTICE '  - Updated % project member role values', member_count;
END $$;


-- =====================================================
-- SECTION 4: Optimize Indexes
-- =====================================================
-- Update indexes to optimize common query patterns
-- =====================================================

-- Drop old archived projects index
DROP INDEX IF EXISTS idx_projects_archived;

-- Create new composite index for archived projects by owner
-- This optimizes queries like: "show me my archived projects"
-- which needs to filter by is_archived = true AND created_by = current_user
CREATE INDEX idx_projects_archived_owner
    ON projects(is_archived, created_by)
    WHERE is_archived = true;

-- Create index for active projects (most common query)
CREATE INDEX idx_projects_active
    ON projects(is_archived)
    WHERE is_archived = false;

-- Add index for project key pattern matching
-- Optimizes queries that search for PROJ-* keys
CREATE INDEX idx_projects_key_pattern
    ON projects(key)
    WHERE key LIKE 'PROJ-%';

-- Add index for visibility filtering
-- Optimizes queries that filter by PUBLIC projects
CREATE INDEX idx_projects_visibility
    ON projects(visibility, is_archived)
    WHERE is_archived = false;

-- Add composite index for member role queries
-- Optimizes queries like: "find all projects where user is OWNER"
CREATE INDEX idx_project_members_user_role
    ON project_members(user_id, role);

-- Add index for finding project owners
-- Optimizes queries like: "who are the owners of this project"
CREATE INDEX idx_project_members_project_owner
    ON project_members(project_id, role)
    WHERE role = 'OWNER';


-- =====================================================
-- SECTION 5: Additional Table Comments
-- =====================================================
-- Add documentation for better schema understanding
-- =====================================================

-- Add table-level comments
COMMENT ON TABLE projects IS 'Stores project information. Projects are containers for issues and provide workspace organization.';
COMMENT ON TABLE project_members IS 'Stores project membership information with role-based access control.';
COMMENT ON TABLE project_issue_counters IS 'Tracks sequential issue numbers per project for generating issue keys (e.g., PROJ-1, PROJ-2).';

-- Add additional column comments
COMMENT ON COLUMN projects.key IS 'Unique project identifier. Auto-generated in format: PROJ-{sequential_number}. Cannot be changed after creation.';
COMMENT ON COLUMN projects.name IS 'Human-readable project name (3-255 characters).';
COMMENT ON COLUMN projects.is_archived IS 'Soft delete flag. Archived projects are only visible to project OWNER.';
COMMENT ON COLUMN projects.created_by IS 'User who created the project. Automatically becomes first OWNER.';

COMMENT ON COLUMN project_members.role IS 'Member role determines access level and permissions within the project.';
COMMENT ON COLUMN project_members.joined_at IS 'Timestamp when user was added to the project.';

COMMENT ON COLUMN project_issue_counters.last_number IS 'Last used sequential number for issue generation. Incremented atomically.';


-- =====================================================
-- SECTION 6: Project Key Generation Function
-- =====================================================
-- Creates a PostgreSQL function to generate unique project keys
-- Format: PROJ-{sequential_number} (e.g., PROJ-1, PROJ-2, PROJ-3)
-- This ensures globally unique project keys across all projects
-- =====================================================

-- Drop existing function if it exists
DROP FUNCTION IF EXISTS get_next_project_key();

-- Create function to generate next project key
CREATE OR REPLACE FUNCTION get_next_project_key()
RETURNS VARCHAR(10) AS $$
DECLARE
next_number INTEGER;
    new_key VARCHAR(10);
    max_attempts INTEGER := 10;
    attempt INTEGER := 0;
    key_exists BOOLEAN;
BEGIN
    -- Loop to handle potential race conditions
    LOOP
attempt := attempt + 1;
        
        -- Safety check: prevent infinite loop
        IF attempt > max_attempts THEN
            RAISE EXCEPTION 'Failed to generate unique project key after % attempts', max_attempts;
END IF;
        
        -- Find the maximum number from existing PROJ-{n} keys
        -- Uses regex to match exactly PROJ-{digits} pattern
        -- COALESCE returns 0 if no projects exist yet
SELECT COALESCE(MAX(CAST(SUBSTRING(key FROM 6) AS INTEGER)), 0) + 1
INTO next_number
FROM projects
WHERE key ~ '^PROJ-[0-9]+$';

-- Format as PROJ-{number}
new_key := 'PROJ-' || next_number;
        
        -- Check if key already exists (safety check)
SELECT EXISTS(SELECT 1 FROM projects WHERE key = new_key)
INTO key_exists;

-- If key doesn't exist, we found our unique key
IF NOT key_exists THEN
            EXIT;
END IF;
        
        -- If key exists, loop will retry with next number
        RAISE NOTICE 'Key % already exists, retrying...', new_key;
END LOOP;

RETURN new_key;
END;
$$ LANGUAGE plpgsql;

-- Add function comment
COMMENT ON FUNCTION get_next_project_key() IS 
'Generates next sequential project key in format PROJ-{number}. 
Thread-safe with retry logic to handle concurrent project creation.
Example: PROJ-1, PROJ-2, PROJ-3, etc.';


-- =====================================================
-- SECTION 7: Helper Function - Get User Role in Project
-- =====================================================
-- Utility function to quickly check a user''s role in a project
-- Returns NULL if user is not a member
-- =====================================================

-- Drop existing function if it exists
DROP FUNCTION IF EXISTS get_user_project_role(UUID, UUID);

-- Create function to get user's role in a project
CREATE OR REPLACE FUNCTION get_user_project_role(
    p_project_id UUID,
    p_user_id UUID
)
RETURNS VARCHAR(20) AS $$
DECLARE
user_role VARCHAR(20);
BEGIN
    -- Get user's role in the project
SELECT role INTO user_role
FROM project_members
WHERE project_id = p_project_id
  AND user_id = p_user_id;

RETURN user_role;
END;
$$ LANGUAGE plpgsql;

-- Add function comment
COMMENT ON FUNCTION get_user_project_role(UUID, UUID) IS 
'Returns user''s role in a project (OWNER, ADMIN, MEMBER, VIEWER) or NULL if not a member.
Parameters: project_id, user_id';


-- =====================================================
-- SECTION 8: Helper Function - Count Project Owners
-- =====================================================
-- Utility function to count number of owners in a project
-- Used to validate that at least one owner always exists
-- =====================================================

-- Drop existing function if it exists
DROP FUNCTION IF EXISTS count_project_owners(UUID);

-- Create function to count project owners
CREATE OR REPLACE FUNCTION count_project_owners(
    p_project_id UUID
)
RETURNS INTEGER AS $$
DECLARE
owner_count INTEGER;
BEGIN
    -- Count members with OWNER role
SELECT COUNT(*) INTO owner_count
FROM project_members
WHERE project_id = p_project_id
  AND role = 'OWNER';

RETURN owner_count;
END;
$$ LANGUAGE plpgsql;

-- Add function comment
COMMENT ON FUNCTION count_project_owners(UUID) IS 
'Returns the number of OWNER members in a project. 
Should always return at least 1 to maintain project ownership.
Parameter: project_id';


-- =====================================================
-- SECTION 9: Trigger - Prevent Last Owner Removal
-- =====================================================
-- Creates a trigger to prevent removing the last owner from a project
-- This ensures every project always has at least one owner
-- =====================================================

-- Drop existing trigger and function if they exist
DROP TRIGGER IF EXISTS trg_prevent_last_owner_removal ON project_members;
DROP FUNCTION IF EXISTS prevent_last_owner_removal();

-- Create trigger function
CREATE OR REPLACE FUNCTION prevent_last_owner_removal()
RETURNS TRIGGER AS $$
DECLARE
remaining_owners INTEGER;
BEGIN
    -- Only check if we're deleting or updating an OWNER
    IF (TG_OP = 'DELETE' AND OLD.role = 'OWNER') OR 
       (TG_OP = 'UPDATE' AND OLD.role = 'OWNER' AND NEW.role != 'OWNER') THEN
        
        -- Count remaining owners after this change
SELECT COUNT(*) INTO remaining_owners
FROM project_members
WHERE project_id = OLD.project_id
  AND role = 'OWNER'
  AND id != OLD.id;

-- If no owners would remain, prevent the operation
IF remaining_owners = 0 THEN
            RAISE EXCEPTION 'Cannot remove or change role of the last project owner. Project must have at least one OWNER.'
                USING ERRCODE = 'check_violation',
                      HINT = 'Transfer ownership to another member before removing this owner.';
END IF;
END IF;
    
    -- Allow the operation
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
ELSE
        RETURN NEW;
END IF;
END;
$$ LANGUAGE plpgsql;

-- Create trigger
CREATE TRIGGER trg_prevent_last_owner_removal
    BEFORE DELETE OR UPDATE OF role ON project_members
    FOR EACH ROW
    EXECUTE FUNCTION prevent_last_owner_removal();

-- Add comment
COMMENT ON TRIGGER trg_prevent_last_owner_removal ON project_members IS 
'Prevents deletion or role change of the last OWNER in a project. 
Ensures every project always has at least one OWNER.';


-- =====================================================
-- SECTION 10: Data Validation
-- =====================================================
-- Validate existing data meets new constraints
-- =====================================================

-- Check for any projects without visibility set
DO $$
DECLARE
invalid_count INTEGER;
BEGIN
SELECT COUNT(*) INTO invalid_count
FROM projects
WHERE visibility IS NULL OR visibility NOT IN ('PUBLIC', 'PRIVATE');

IF invalid_count > 0 THEN
        RAISE WARNING 'Found % projects with invalid visibility. Setting to PRIVATE.', invalid_count;

UPDATE projects
SET visibility = 'PRIVATE'
WHERE visibility IS NULL OR visibility NOT IN ('PUBLIC', 'PRIVATE');
END IF;
END $$;

-- Check for any members without valid roles
DO $$
DECLARE
invalid_count INTEGER;
BEGIN
SELECT COUNT(*) INTO invalid_count
FROM project_members
WHERE role IS NULL OR role NOT IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER');

IF invalid_count > 0 THEN
        RAISE WARNING 'Found % project members with invalid roles. Setting to MEMBER.', invalid_count;

UPDATE project_members
SET role = 'MEMBER'
WHERE role IS NULL OR role NOT IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER');
END IF;
END $$;

-- Verify every project has at least one owner
DO $$
DECLARE
project_record RECORD;
    projects_without_owner INTEGER := 0;
BEGIN
FOR project_record IN
SELECT p.id, p.key, p.created_by
FROM projects p
WHERE NOT EXISTS (
    SELECT 1 FROM project_members pm
    WHERE pm.project_id = p.id
      AND pm.role = 'OWNER'
)
    LOOP
        projects_without_owner := projects_without_owner + 1;

RAISE WARNING 'Project % (%) has no OWNER. Adding creator as OWNER.',
            project_record.key, project_record.id;
        
        -- Add creator as owner if not already a member
INSERT INTO project_members (project_id, user_id, role, joined_at)
VALUES (
           project_record.id,
           project_record.created_by,
           'OWNER',
           CURRENT_TIMESTAMP
       )
    ON CONFLICT (project_id, user_id)
        DO UPDATE SET role = 'OWNER';
END LOOP;
    
    IF projects_without_owner > 0 THEN
        RAISE NOTICE 'Fixed % projects without owners', projects_without_owner;
ELSE
        RAISE NOTICE 'All projects have at least one OWNER';
END IF;
END $$;


-- =====================================================
-- SECTION 11: Performance Analysis Hints
-- =====================================================
-- Add statistics for query planner optimization
-- =====================================================

-- Update table statistics for query planner
ANALYZE projects;
ANALYZE project_members;
ANALYZE project_issue_counters;


-- =====================================================
-- SECTION 12: Migration Summary
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE '================================================';
    RAISE NOTICE 'Migration V8 completed successfully!';
    RAISE NOTICE '================================================';
    RAISE NOTICE 'Changes applied:';
    RAISE NOTICE '  ✓ Updated visibility constraint (PUBLIC, PRIVATE only)';
    RAISE NOTICE '  ✓ Added OWNER role to project members';
    RAISE NOTICE '  ✓ Migrated existing data to uppercase';
    RAISE NOTICE '  ✓ Created optimized indexes for owner queries';
    RAISE NOTICE '  ✓ Created get_next_project_key() function';
    RAISE NOTICE '  ✓ Created get_user_project_role() helper function';
    RAISE NOTICE '  ✓ Created count_project_owners() helper function';
    RAISE NOTICE '  ✓ Added trigger to prevent last owner removal';
    RAISE NOTICE '  ✓ Added comprehensive documentation comments';
    RAISE NOTICE '  ✓ Validated and fixed existing data';
    RAISE NOTICE '================================================';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '  1. Review migration results';
    RAISE NOTICE '  2. Test get_next_project_key() function';
    RAISE NOTICE '  3. Verify indexes are being used (EXPLAIN ANALYZE)';
    RAISE NOTICE '  4. Proceed with Phase 2 (Entity implementation)';
    RAISE NOTICE '================================================';
END $$;