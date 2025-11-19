-- Issues Table
CREATE TABLE issues (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
                        key VARCHAR(50) NOT NULL UNIQUE,
                        due_date DATE,
                        sequential_number INTEGER NOT NULL,
                        title VARCHAR(500) NOT NULL,
                        description TEXT,
                        type VARCHAR(20) NOT NULL DEFAULT 'task',
                        priority VARCHAR(20) NOT NULL DEFAULT 'medium',
                        status UUID NOT NULL REFERENCES workflow_statuses(id),
                        reporter_id UUID NOT NULL REFERENCES users(id),
                        assignee_id UUID REFERENCES users(id),
                        is_deleted BOOLEAN NOT NULL DEFAULT false,
                        deleted_by UUID REFERENCES users(id),
                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        resolved_at TIMESTAMPTZ,
                        closed_at TIMESTAMPTZ,
                        deleted_at TIMESTAMPTZ,
                        CONSTRAINT chk_issue_type CHECK (type IN ('task', 'bug')),
                        CONSTRAINT chk_issue_priority CHECK (priority IN ('low', 'medium', 'high')),
                        UNIQUE(project_id, sequential_number)
);

-- Issue History Table
CREATE TABLE issue_history (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
                               changed_by UUID NOT NULL REFERENCES users(id),
                               field VARCHAR(100) NOT NULL,
                               old_value TEXT,
                               new_value TEXT,
                               changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE UNIQUE INDEX idx_issues_key ON issues(key);
CREATE INDEX idx_issues_project_id ON issues(project_id);
CREATE INDEX idx_issues_assignee_id ON issues(assignee_id);
CREATE INDEX idx_issues_reporter_id ON issues(reporter_id);
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_type ON issues(type);
CREATE INDEX idx_issues_priority ON issues(priority);
CREATE INDEX idx_issues_created_at ON issues(created_at DESC);
CREATE INDEX idx_issues_deleted ON issues(is_deleted) WHERE is_deleted = false;
CREATE INDEX idx_issue_history_issue_id ON issue_history(issue_id);
CREATE INDEX idx_issue_history_changed_at ON issue_history(changed_at DESC);