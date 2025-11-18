-- Projects Table
CREATE TABLE projects (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          key VARCHAR(10) NOT NULL UNIQUE,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          visibility VARCHAR(20) NOT NULL DEFAULT 'private',
                          created_by UUID NOT NULL REFERENCES users(id),
                          is_archived BOOLEAN NOT NULL DEFAULT false,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          archived_at TIMESTAMPTZ,
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT chk_visibility CHECK (visibility IN ('public', 'private', 'internal'))
);

-- Project Members Table (with UUID PK)
CREATE TABLE project_members (
                                 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
                                 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                 role VARCHAR(20) NOT NULL DEFAULT 'member',
                                 joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT chk_project_role CHECK (role IN ('admin', 'member', 'viewer')),
                                 UNIQUE(project_id, user_id)
);

-- Project Issue Counters Table
CREATE TABLE project_issue_counters (
                                        project_id UUID PRIMARY KEY REFERENCES projects(id) ON DELETE CASCADE,
                                        last_number INTEGER NOT NULL DEFAULT 0
);

-- Indexes
CREATE INDEX idx_projects_key ON projects(key);
CREATE INDEX idx_projects_created_by ON projects(created_by);
CREATE INDEX idx_projects_archived ON projects(is_archived) WHERE is_archived = false;
CREATE INDEX idx_project_members_project_id ON project_members(project_id);
CREATE INDEX idx_project_members_user_id ON project_members(user_id);