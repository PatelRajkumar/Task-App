
-- Activity Feed Table
CREATE TABLE activity_feed (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
                               issue_id UUID REFERENCES issues(id) ON DELETE CASCADE,
                               actor_id UUID NOT NULL REFERENCES users(id),
                               action VARCHAR(50) NOT NULL,
                               payload JSONB,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_activity_feed_project_id ON activity_feed(project_id, created_at DESC);
CREATE INDEX idx_activity_feed_issue_id ON activity_feed(issue_id, created_at DESC);
CREATE INDEX idx_activity_feed_actor_id ON activity_feed(actor_id);
CREATE INDEX idx_activity_feed_created_at ON activity_feed(created_at DESC);
