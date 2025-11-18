-- Create extension for UUID support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users Table
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       avatar_url VARCHAR(1000),
                       name VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT true,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       last_login_at TIMESTAMPTZ
);

-- Roles Table
CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description TEXT
);

-- User Roles Junction Table (Composite PK)
CREATE TABLE user_roles (
                            user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

-- Refresh Tokens Table
CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                token VARCHAR(500) NOT NULL UNIQUE,
                                expires_at TIMESTAMPTZ NOT NULL
);

-- Permissions Table
CREATE TABLE permissions (
                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             code VARCHAR(100) NOT NULL UNIQUE,
                             description TEXT
);

-- Role Permissions Junction Table (Composite PK)
CREATE TABLE role_permissions (
                                  role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                                  permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
                                  PRIMARY KEY (role_id, permission_id)
);

-- Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_enabled ON users(enabled) WHERE enabled = true;
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Insert default roles
INSERT INTO roles (id, name, description) VALUES
                                              (uuid_generate_v4(), 'ROLE_ADMIN', 'System administrator with full access'),
                                              (uuid_generate_v4(), 'ROLE_USER', 'Regular user with standard access');

-- Insert default permissions
INSERT INTO permissions (id, code, description) VALUES
                                                    (uuid_generate_v4(), 'USER_READ', 'Can view users'),
                                                    (uuid_generate_v4(), 'USER_WRITE', 'Can create and update users'),
                                                    (uuid_generate_v4(), 'USER_DELETE', 'Can delete users'),
                                                    (uuid_generate_v4(), 'PROJECT_READ', 'Can view projects'),
                                                    (uuid_generate_v4(), 'PROJECT_WRITE', 'Can create and update projects'),
                                                    (uuid_generate_v4(), 'PROJECT_DELETE', 'Can delete projects'),
                                                    (uuid_generate_v4(), 'ISSUE_READ', 'Can view issues'),
                                                    (uuid_generate_v4(), 'ISSUE_WRITE', 'Can create and update issues'),
                                                    (uuid_generate_v4(), 'ISSUE_DELETE', 'Can delete issues');