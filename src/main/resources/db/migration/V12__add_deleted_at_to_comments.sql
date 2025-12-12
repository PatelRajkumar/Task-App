-- =====================================================
-- V12: Add deleted_at column to comments table
-- =====================================================
-- Adds missing deleted_at timestamp column to support
-- soft delete audit trail for comments
-- =====================================================

-- Add deleted_at column to comments table
ALTER TABLE comments 
ADD COLUMN deleted_at TIMESTAMPTZ;

-- Add index for soft-deleted comments queries
CREATE INDEX idx_comments_deleted_at ON comments(deleted_at) WHERE deleted_at IS NOT NULL;

-- Migration complete
DO $$
BEGIN
    RAISE NOTICE '================================================';
    RAISE NOTICE 'Migration V12 completed successfully!';
    RAISE NOTICE 'Added deleted_at column to comments table';
    RAISE NOTICE '================================================';
END $$;
