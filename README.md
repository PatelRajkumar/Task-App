# TaskApp – Jira-like Task Management System (Spring Boot + Postgres)

TaskApp is a modular, Jira-inspired task & project management backend built with  
**Spring Boot, PostgreSQL, JPA, JWT authentication, and clean modular architecture**.

This is a backend API-only application.

---

## 🚀 Features (Planned & In Progress)

### ✅ Completed
- **User Module**
  - User registration
  - Secure password hashing
  - UserResponse DTO mapping
  - Validation & global exception handling
  - Duplicate email check with proper 409 response  

### 🚧 In Progress (Next)
- **Authentication & Authorization (JWT + Refresh Tokens)**
- **Projects**
- **Issues / Tasks**
- **Comments**
- **Labels & Tags**
- **Attachments**
- **Workflow & Statuses**
- **Activity Feed (Audit)**
- **Search (Full-text, Postgres GIN)**
- **Permissions (Role-based + Project-level)**

---

## Database Migrations

This project uses Flyway for database version control.

### First Time Setup

1. Start PostgreSQL:
```bash
docker-compose up -d
```

2. Run the application (migrations auto-apply):
```bash
mvn spring-boot:run
```

3. Verify migrations:
```sql
SELECT * FROM flyway_schema_history;
```

### Adding New Migrations

1. Create a new file: `V{next_number}__{description}.sql`
   Example: `V9__add_user_preferences.sql`

2. Write your SQL:
```sql
ALTER TABLE users ADD COLUMN preferences JSONB;
```

3. Run application - Flyway auto-applies new migration

### Important Rules

- ❌ NEVER modify existing migration files
- ✅ Always create new migration for changes
- ✅ Test migrations in dev before production
- ✅ Keep migrations idempotent when possible

## 🧱 Architecture

This project uses a **package-by-feature** structure:

