-- Remove Activity Feed (redundant with Issue History)

-- Drop foreign key constraints first
ALTER TABLE activity_feed DROP CONSTRAINT IF EXISTS activity_feed_actor_id_fkey;
ALTER TABLE activity_feed DROP CONSTRAINT IF EXISTS activity_feed_issue_id_fkey;
ALTER TABLE activity_feed DROP CONSTRAINT IF EXISTS activity_feed_project_id_fkey;

-- Drop indexes
DROP INDEX IF EXISTS idx_activity_feed_project_id;
DROP INDEX IF EXISTS idx_activity_feed_issue_id;
DROP INDEX IF EXISTS idx_activity_feed_actor_id;
DROP INDEX IF EXISTS idx_activity_feed_created_at;

-- Drop table
DROP TABLE IF EXISTS activity_feed;