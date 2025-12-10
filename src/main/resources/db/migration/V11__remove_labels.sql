-- =====================================================
-- V11: Remove Labels and Issue_Labels Tables
-- =====================================================
-- This migration removes the labels functionality
-- as it's redundant with existing type and priority
-- =====================================================

-- =====================================================
-- SECTION 1: Drop Tables
-- =====================================================

DO $$
BEGIN
    -- Drop issue_labels junction table first (has FK to labels)
    IF EXISTS (SELECT 1 FROM information_schema.tables 
               WHERE table_schema = 'public' 
               AND table_name = 'issue_labels') THEN
        DROP TABLE issue_labels CASCADE;
        RAISE NOTICE 'Dropped table: issue_labels';
    ELSE
        RAISE NOTICE 'Table issue_labels does not exist';
    END IF;

    -- Drop labels table
    IF EXISTS (SELECT 1 FROM information_schema.tables 
               WHERE table_schema = 'public' 
               AND table_name = 'labels') THEN
        DROP TABLE labels CASCADE;
        RAISE NOTICE 'Dropped table: labels';
    ELSE
        RAISE NOTICE 'Table labels does not exist';
    END IF;
END $$;


-- =====================================================
-- SECTION 2: Verify Tables Are Dropped
-- =====================================================

DO $$
DECLARE
    label_table_count INTEGER;
BEGIN
    -- Count remaining label-related tables
    SELECT COUNT(*) INTO label_table_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
    AND table_name IN ('labels', 'issue_labels');

    IF label_table_count = 0 THEN
        RAISE NOTICE '================================================';
        RAISE NOTICE 'SUCCESS! Labels tables removed.';
        RAISE NOTICE '================================================';
    ELSE
        RAISE WARNING 'Still found % label-related tables', label_table_count;
    END IF;
END $$;