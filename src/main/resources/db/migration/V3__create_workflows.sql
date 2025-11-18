-- Workflows Table (all tied to projects)
CREATE TABLE workflows (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
                           name VARCHAR(100) NOT NULL
);

-- Workflow Statuses Table
CREATE TABLE workflow_statuses (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   workflow_id UUID NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
                                   code VARCHAR(50) NOT NULL,
                                   color VARCHAR(20),
                                   name VARCHAR(100) NOT NULL,
                                   position INTEGER NOT NULL,
                                   UNIQUE(workflow_id, code)
);

-- Workflow Transitions Table
CREATE TABLE workflow_transitions (
                                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                      workflow_id UUID NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
                                      from_code VARCHAR(50) NOT NULL,
                                      to_code VARCHAR(50) NOT NULL,
                                      is_automatic BOOLEAN NOT NULL DEFAULT false,
                                      UNIQUE(workflow_id, from_code, to_code)
);

-- Indexes
CREATE INDEX idx_workflows_project_id ON workflows(project_id);
CREATE INDEX idx_workflow_statuses_workflow_id ON workflow_statuses(workflow_id);
CREATE INDEX idx_workflow_statuses_code ON workflow_statuses(workflow_id, code);
CREATE INDEX idx_workflow_transitions_workflow_id ON workflow_transitions(workflow_id);