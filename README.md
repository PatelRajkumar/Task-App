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
  
- **Authentication & Authorization**
  - JWT token-based authentication
  - Refresh token mechanism
  - Password reset functionality
  - Email verification
  - Role-based access control (RBAC)
  - Permission management
  
- **Projects Module**
  - Project CRUD operations
  - Project member management (OWNER, ADMIN, MEMBER, VIEWER roles)
  - Project visibility (PUBLIC/PRIVATE)
  - Archive/restore functionality
  - Project search and filtering
  - Ownership transfer

### 🚧 In Progress (Next)
- **Issues / Tasks Module**
  - Simple status workflow (TODO → INPROGRESS → DONE)
  - Issue types (task, bug)
  - Priority levels (low, medium, high)
  - Assignment and tracking

### 📋 Planned
- **Comments**
- **Labels & Tags**
- **Attachments**
- **Activity Feed (Audit)**
- **Search (Full-text, Postgres GIN)**

---

## 🏗️ Architecture Decisions

### Issue Status Management
This application uses a **simplified status system** rather than complex workflows:
- **Three statuses**: TODO, INPROGRESS, DONE
- **Linear progression**: Issues move sequentially through states
- **Enum-based**: Status stored as VARCHAR with CHECK constraint
- **Rationale**: Keeps the system simple and maintainable for the core use case

This decision avoids over-engineering while still providing effective task tracking. If complex workflows are needed in the future, they can be added through a migration.

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
   Example: `V10__add_user_preferences.sql`

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

### Migration History

- **V1**: Users, Roles, and Permissions
- **V2**: Projects and Project Members
- **V3**: ~~Workflows~~ (Removed in V9)
- **V4**: Issues and Issue History
- **V5**: Comments and Attachments
- **V6**: Labels
- **V7**: Activity Feed
- **V8**: Projects Schema Updates
- **V9**: Remove Workflows (Simplified to status enum)
- **V10**: Update Enum Constraints (uppercase STATUS, TYPE, PRIORITY)
- **V11**: Remove Labels (Simplified - redundant with type/priority)
- **V12**: Add Deleted at to comments table
- **V13**: Fix attachments storage type enum
- **V14**: Remove Activity Feed (Redundant with Issue History)

---

## 🧱 Architecture

This project uses a **package-by-feature** structure:

```
com.pm.taskapp/
├── auth/              # Authentication & authorization
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── security/
├── project/           # Project management
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── enums/
├── issue/             # Issues/Tasks (TODO)
├── comment/           # Comments (TODO)
└── common/            # Shared utilities
    ├── exception/
    └── config/
```

---

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL 15+
- **Cache**: Redis 7.x
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA / Hibernate
- **Migration**: Flyway
- **Validation**: Hibernate Validator
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven

---

## 💾 Caching Strategy

The application uses **Redis** for caching to improve performance and reduce database load.

### Cache Configuration

**Location**: `com.pm.taskapp.config.cache.RedisConfig`

- **Serialization**: JSON with Spring Security support
- **Default TTL**: 5 minutes
- **Transaction-aware**: Cache operations synchronized with database transactions

### Cached Data

| Cache Name | Data | TTL | Eviction Triggers |
|------------|------|-----|-------------------|
| `user:details` | UserPrincipal (with authorities) | 15 min | User update, password change, role assignment, logout |

### Cache Eviction

Cache is automatically evicted when:
- User profile is updated
- User password is changed
- User roles or permissions are modified
- User account is enabled/disabled
- User is deleted
- User logs out

### Redis Setup

**Via Docker Compose** (Recommended):
```bash
cd docker/postgres
docker-compose up -d redis
```

**Configuration** (`application-dev.yml`):
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD}
```

### Monitoring Cache

**View cached keys**:
```bash
docker exec -it taskapp_redis redis-cli
KEYS *
```

**Clear all cache** (development only):
```bash
docker exec taskapp_redis redis-cli FLUSHALL
```

### Performance Impact

- **First request**: Loads from database → Caches in Redis
- **Subsequent requests**: Loads from Redis (15-20x faster)
- **After eviction**: Reloads from database → Re-caches

---

## 📝 API Documentation

Once the application is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

---

## 🔐 Authentication

The API uses JWT (JSON Web Token) for authentication:

1. Register: `POST /api/auth/register`
2. Login: `POST /api/auth/login` - Returns access token and refresh token
3. Use token: Include in header: `Authorization: Bearer {token}`
4. Refresh: `POST /api/auth/refresh` - Get new access token

---

## 🌟 Key Features

### Role-Based Access Control
- System-wide roles: ADMIN, USER, MANAGER, GUEST
- Fine-grained permissions for different operations
- Project-level roles: OWNER, ADMIN, MEMBER, VIEWER

### Project Management
- Public and private projects
- Member management with role-based permissions
- Archive/restore functionality
- Project-level issue counters for unique keys

### Issue Tracking (Simplified)
- **Status Flow**: TODO → INPROGRESS → DONE
- **Issue Types**: Task, Bug
- **Priorities**: Low, Medium, High
- Soft delete support
- Due date tracking

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- PostgreSQL 15+
- Maven 3.8+

### Setup

1. Clone the repository
```bash
git clone <repository-url>
cd taskapp
```

2. Configure database (application-dev.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskapp
    username: your_username
    password: your_password
```

3. Run the application
```bash
mvn spring-boot:run
```

---

## ⚠️ Removed Features

### Labels Module (Removed in V11)
- **Reason**: Redundant with existing `type` (TASK/BUG) and `priority` (LOW/MEDIUM/HIGH)
- **Alternative**: Use issue type and priority for categorization
- **Date Removed**: December 2024

### Audit Trail Strategy
This application uses **Issue History** for comprehensive audit tracking:
- **Issue History**: Tracks all field-level changes (status, assignee, priority, title, description)
- **Location**: `issue_history` table
- **Coverage**: Complete change tracking with old/new values, timestamps, and actor
- **Decision**: Activity Feed removed (V12) as redundant with Issue History

## 📧 Contact

For questions or issues, please contact the development team.

---

## 📄 License

[Specify your license here]