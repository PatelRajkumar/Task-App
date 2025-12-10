-- =====================================================
-- V10: Update Enum Constraints to Uppercase
-- =====================================================
-- This migration updates the CHECK constraints for
-- issues table to use uppercase enum values matching
-- the Java enum definitions:
-- - IssueStatus: TODO, INPROGRESS, DONE
-- - IssueType: TASK, BUG  
-- - IssuePriority: LOW, MEDIUM, HIGH
-- =====================================================

-- =====================================================
-- SECTION 1: Update IssueStatus Constraint
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE 'Updating IssueStatus constraint...';
    
    -- Drop old constraint if exists
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_issue_status'
        AND table_name = 'issues'
    ) THEN
        ALTER TABLE issues DROP CONSTRAINT chk_issue_status;
        RAISE NOTICE 'Dropped old chk_issue_status constraint';
    END IF;

    -- Add new constraint with uppercase values
    ALTER TABLE issues 
    ADD CONSTRAINT chk_issue_status 
    CHECK (status IN ('TODO', 'INPROGRESS', 'DONE'));
    
    RAISE NOTICE 'Added new chk_issue_status constraint: TODO, INPROGRESS, DONE';
    
    -- Update existing data from lowercase to uppercase (if any exists)
    UPDATE issues SET status = UPPER(status) WHERE status IS NOT NULL;
    
    RAISE NOTICE 'Updated existing status values to uppercase';
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Error updating IssueStatus: %', SQLERRM;
END $$;


-- =====================================================
-- SECTION 2: Update IssueType Constraint
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE 'Updating IssueType constraint...';
    
    -- Drop old constraint
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_issue_type'
        AND table_name = 'issues'
    ) THEN
        ALTER TABLE issues DROP CONSTRAINT chk_issue_type;
        RAISE NOTICE 'Dropped old chk_issue_type constraint';
    END IF;

    -- Add new constraint with uppercase values
    ALTER TABLE issues 
    ADD CONSTRAINT chk_issue_type 
    CHECK (type IN ('TASK', 'BUG'));
    
    RAISE NOTICE 'Added new chk_issue_type constraint: TASK, BUG';
    
    -- Update existing data from lowercase to uppercase
    UPDATE issues SET type = UPPER(type);
    
    RAISE NOTICE 'Updated existing type values to uppercase';
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Error updating IssueType: %', SQLERRM;
END $$;


-- =====================================================
-- SECTION 3: Update IssuePriority Constraint
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE 'Updating IssuePriority constraint...';
    
    -- Drop old constraint
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_issue_priority'
        AND table_name = 'issues'
    ) THEN
        ALTER TABLE issues DROP CONSTRAINT chk_issue_priority;
        RAISE NOTICE 'Dropped old chk_issue_priority constraint';
    END IF;

    -- Add new constraint with uppercase values
    ALTER TABLE issues 
    ADD CONSTRAINT chk_issue_priority 
    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'));
    
    RAISE NOTICE 'Added new chk_issue_priority constraint: LOW, MEDIUM, HIGH';
    
    -- Update existing data from lowercase to uppercase
    UPDATE issues SET priority = UPPER(priority);
    
    RAISE NOTICE 'Updated existing priority values to uppercase';
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Error updating IssuePriority: %', SQLERRM;
END $$;


-- =====================================================
-- SECTION 4: Update Default Values
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE 'Updating default values...';
    
    -- Update type default to uppercase
    ALTER TABLE issues ALTER COLUMN type SET DEFAULT 'TASK';
    RAISE NOTICE 'Updated type default to TASK';
    
    -- Update priority default to uppercase  
    ALTER TABLE issues ALTER COLUMN priority SET DEFAULT 'MEDIUM';
    RAISE NOTICE 'Updated priority default to MEDIUM';
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Error updating defaults: %', SQLERRM;
END $$;


-- =====================================================
-- SECTION 5: Final Verification
-- =====================================================

DO $$
DECLARE
    type_constraint BOOLEAN;
    priority_constraint BOOLEAN;
    status_constraint BOOLEAN;
    status_type TEXT;
BEGIN
    RAISE NOTICE '================================================';
    RAISE NOTICE 'VERIFICATION REPORT';
    RAISE NOTICE '================================================';
    
    -- Check status column type
    SELECT data_type INTO status_type
    FROM information_schema.columns
    WHERE table_name = 'issues' AND column_name = 'status';
    
    RAISE NOTICE 'Status column type: %', status_type;
    
    -- Check constraints exist
    SELECT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_issue_type' AND table_name = 'issues'
    ) INTO type_constraint;
    
    SELECT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_issue_priority' AND table_name = 'issues'
    ) INTO priority_constraint;
    
    SELECT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_issue_status' AND table_name = 'issues'
    ) INTO status_constraint;
    
    RAISE NOTICE 'Type constraint exists: %', type_constraint;
    RAISE NOTICE 'Priority constraint exists: %', priority_constraint;
    RAISE NOTICE 'Status constraint exists: %', status_constraint;
    
    IF status_type = 'character varying' AND 
       type_constraint AND 
       priority_constraint AND 
       status_constraint THEN
        RAISE NOTICE '================================================';
        RAISE NOTICE 'SUCCESS! All enum constraints updated.';
        RAISE NOTICE '================================================';
    ELSE
        RAISE NOTICE '================================================';
        RAISE NOTICE 'PARTIAL SUCCESS - Check warnings above';
        RAISE NOTICE '================================================';
    END IF;
END $$;