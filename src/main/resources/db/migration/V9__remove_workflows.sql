-- =====================================================
-- V10: Force Drop Workflow Tables
-- =====================================================
-- This migration forcefully drops workflow tables that
-- still exist after V9 migration
-- =====================================================

-- =====================================================
-- SECTION 1: Verify and Drop Workflow Tables
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
-- SECTION 2: Verify issues.status Column
-- =====================================================
-- Ensure issues table has correct status column

DO $$
DECLARE
    status_type TEXT;
BEGIN
    -- Check current type of status column
    SELECT data_type INTO status_type
    FROM information_schema.columns
    WHERE table_name = 'issues' 
    AND column_name = 'status';

    IF status_type = 'character varying' THEN
        RAISE NOTICE 'Issues.status is already VARCHAR - correct!';
    ELSIF status_type = 'uuid' THEN
        RAISE EXCEPTION 'Issues.status is still UUID! Manual intervention needed.';
    ELSE
        RAISE NOTICE 'Issues.status type is: %', status_type;
    END IF;
END $$;


-- =====================================================
-- SECTION 3: Verify Status Constraint Exists
-- =====================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_issue_status'
        AND table_name = 'issues'
    ) THEN
        RAISE NOTICE 'Status constraint exists - correct!';
    ELSE
        RAISE WARNING 'Status constraint does not exist - adding it now';
        ALTER TABLE issues 
        ADD CONSTRAINT chk_issue_status 
        CHECK (status IN ('TODO', 'INPROGRESS', 'DONE'));
    END IF;
END $$;


-- =====================================================
-- SECTION 4: Final Verification
-- =====================================================

DO $$
DECLARE
    workflow_count INTEGER;
BEGIN
    -- Count remaining workflow tables
    SELECT COUNT(*) INTO workflow_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
    AND table_name LIKE 'workflow%';

    IF workflow_count = 0 THEN
        RAISE NOTICE '================================================';
        RAISE NOTICE 'SUCCESS! All workflow tables removed.';
        RAISE NOTICE '================================================';
    ELSE
        RAISE WARNING 'Still found % workflow-related tables', workflow_count;
    END IF;
END $$;