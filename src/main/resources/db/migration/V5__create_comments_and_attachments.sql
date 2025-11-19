-- Comments Table
CREATE TABLE comments (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
                          author_id UUID NOT NULL REFERENCES users(id),
                          content TEXT NOT NULL,
                          is_deleted BOOLEAN NOT NULL DEFAULT false,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Attachments Table
CREATE TABLE attachments (
                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
                             original_filename VARCHAR(255) NOT NULL,
                             file_size BIGINT NOT NULL,
                             mime_type VARCHAR(100),
                             uploaded_by UUID NOT NULL REFERENCES users(id),
                             filename VARCHAR(255) NOT NULL,
                             storage_path VARCHAR(1000) NOT NULL,
                             storage_type VARCHAR(20) NOT NULL DEFAULT 'local',
                             is_deleted BOOLEAN NOT NULL DEFAULT false,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT chk_storage_type CHECK (storage_type IN ('local', 's3', 'azure'))
);

-- Indexes
CREATE INDEX idx_comments_issue_id ON comments(issue_id);
CREATE INDEX idx_comments_author_id ON comments(author_id);
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);
CREATE INDEX idx_attachments_issue_id ON attachments(issue_id);
CREATE INDEX idx_attachments_uploaded_by ON attachments(uploaded_by);