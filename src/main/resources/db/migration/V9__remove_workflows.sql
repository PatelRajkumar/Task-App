-- V9: Remove Workflow Tables and Normalize Issue Status
-- =====================================================
-- This migration removes workflow tables and converts
-- issues.status from UUID (FK) to VARCHAR enum
-- =====================================================

-- =====================================================
-- SECTION 1: Backup existing status data
-- =====================================================

DO $$
BEGIN
    -- Create temporary mapping table if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_schema = 'public' 
                   AND table_name = 'temp_status_mapping') THEN
        
        CREATE TEMP TABLE temp_status_mapping AS
        SELECT 
            i.id as issue_id,
            COALESCE(ws.name, 'TODO') as status_name
        FROM issues i
        LEFT JOIN workflow_statuses ws ON i.status = ws.id;
        
        RAISE NOTICE 'Created temporary status mapping for % issues', 
            (SELECT COUNT(*) FROM temp_status_mapping);
    END IF;
END $$;


-- =====================================================
-- SECTION 2: Drop Foreign Key Constraint
-- =====================================================

DO $$
BEGIN
    -- Drop foreign key constraint from issues to workflow_statuses
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'issues_status_fkey'
        AND table_name = 'issues'
    ) THEN
        ALTER TABLE issues DROP CONSTRAINT issues_status_fkey;
        RAISE NOTICE 'Dropped constraint: issues_status_fkey';
    END IF;
    
    -- Drop any other workflow-related FK constraints
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_issues_workflow_status'
        AND table_name = 'issues'
    ) THEN
        ALTER TABLE issues DROP CONSTRAINT fk_issues_workflow_status;
        RAISE NOTICE 'Dropped constraint: fk_issues_workflow_status';
    END IF;
END $$;


-- =====================================================
-- SECTION 3: Convert status column from UUID to VARCHAR
-- =====================================================

DO $$
BEGIN
    -- Add new varchar column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'issues'
        AND column_name = 'status_temp'
    ) THEN
        ALTER TABLE issues ADD COLUMN status_temp VARCHAR(50);
        RAISE NOTICE 'Added status_temp VARCHAR column';
    END IF;
    
    -- Migrate data using the temporary mapping
    UPDATE issues i
    SET status_temp = tm.status_name
    FROM temp_status_mapping tm
    WHERE i.id = tm.issue_id;
    
    RAISE NOTICE 'Migrated status data to status_temp';
    
    -- Drop old UUID status column
    ALTER TABLE issues DROP COLUMN status;
    RAISE NOTICE 'Dropped old UUID status column';
    
    -- Rename status_temp to status
    ALTER TABLE issues RENAME COLUMN status_temp TO status;
    RAISE NOTICE 'Renamed status_temp to status';
    
    -- Set NOT NULL constraint
    ALTER TABLE issues ALTER COLUMN status SET NOT NULL;
    RAISE NOTICE 'Set status column as NOT NULL';
END $$;


-- =====================================================
-- SECTION 4: Add CHECK Constraint
-- =====================================================

DO $$
BEGIN
    -- Add constraint only if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_issue_status'
    ) THEN
        ALTER TABLE issues
        ADD CONSTRAINT chk_issue_status
        CHECK (status IN ('TODO', 'INPROGRESS', 'DONE'));
        RAISE NOTICE 'Added chk_issue_status constraint';
    ELSE
        RAISE NOTICE 'Constraint chk_issue_status already exists';
    END IF;
END $$;


-- =====================================================
-- SECTION 5: Set default value
-- =====================================================

DO $$
BEGIN
    ALTER TABLE issues ALTER COLUMN status SET DEFAULT 'TODO';
    RAISE NOTICE 'Set default value for status column';
END $$;


-- =====================================================
-- SECTION 6: Drop Workflow Tables
-- =====================================================

DO $$
BEGIN
    -- Drop workflow_transitions table
    IF EXISTS (SELECT 1 FROM information_schema.tables 
               WHERE table_schema = 'public' 
               AND table_name = 'workflow_transitions') THEN
        DROP TABLE workflow_transitions CASCADE;
        RAISE NOTICE 'Dropped table: workflow_transitions';
    ELSE
        RAISE NOTICE 'Table workflow_transitions does not exist';
    END IF;

    -- Drop workflow_statuses table
    IF EXISTS (SELECT 1 FROM information_schema.tables 
               WHERE table_schema = 'public' 
               AND table_name = 'workflow_statuses') THEN
        DROP TABLE workflow_statuses CASCADE;
        RAISE NOTICE 'Dropped table: workflow_statuses';
    ELSE
        RAISE NOTICE 'Table workflow_statuses does not exist';
    END IF;

    -- Drop workflows table
    IF EXISTS (SELECT 1 FROM information_schema.tables 
               WHERE table_schema = 'public' 
               AND table_name = 'workflows') THEN
        DROP TABLE workflows CASCADE;
        RAISE NOTICE 'Dropped table: workflows';
    ELSE
        RAISE NOTICE 'Table workflows does not exist';
    END IF;
END $$;


-- =====================================================
-- SECTION 7: Create Index on Status
-- =====================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'idx_issues_status'
    ) THEN
        CREATE INDEX idx_issues_status ON issues(status);
        RAISE NOTICE 'Created index: idx_issues_status';
    END IF;
END $$;


-- =====================================================
-- SECTION 8: Final Verification
-- =====================================================

DO $$
DECLARE
    workflow_count INTEGER;
    status_type TEXT;
    issue_count INTEGER;
BEGIN
    -- Count remaining workflow tables
    SELECT COUNT(*) INTO workflow_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
    AND table_name LIKE 'workflow%';

    -- Check status column type
    SELECT data_type INTO status_type
    FROM information_schema.columns
    WHERE table_name = 'issues'
    AND column_name = 'status';
    
    -- Count issues
    SELECT COUNT(*) INTO issue_count FROM issues;

    RAISE NOTICE '================================================';
    RAISE NOTICE 'Migration V9 Complete!';
    RAISE NOTICE '================================================';
    RAISE NOTICE 'Workflow tables remaining: %', workflow_count;
    RAISE NOTICE 'Issues.status type: %', status_type;
    RAISE NOTICE 'Total issues migrated: %', issue_count;
    RAISE NOTICE '================================================';
END $$;