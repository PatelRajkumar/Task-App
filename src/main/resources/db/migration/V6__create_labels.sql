-- Labels Table (Global labels)
CREATE TABLE labels (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        name VARCHAR(50) NOT NULL UNIQUE,
                        color VARCHAR(7) NOT NULL
);

-- Issue Labels Junction Table (Composite PK)
CREATE TABLE issue_labels (
                              issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
                              label_id UUID NOT NULL REFERENCES labels(id) ON DELETE CASCADE,
                              added_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (issue_id, label_id)
);

-- Indexes
CREATE INDEX idx_labels_name ON labels(name);
CREATE INDEX idx_issue_labels_issue_id ON issue_labels(issue_id);
CREATE INDEX idx_issue_labels_label_id ON issue_labels(label_id);

-- Insert some default labels
INSERT INTO labels (id, name, color) VALUES
                                         (uuid_generate_v4(), 'bug', '#d73a4a'),
                                         (uuid_generate_v4(), 'enhancement', '#a2eeef'),
                                         (uuid_generate_v4(), 'documentation', '#0075ca'),
                                         (uuid_generate_v4(), 'urgent', '#b60205'),
                                         (uuid_generate_v4(), 'help wanted', '#008672');