# TaskApp – Jira-like Task Management System (Spring Boot + PostgreSQL)

TaskApp is a modular, Jira-inspired task & project management backend built with  
**Spring Boot, PostgreSQL, JPA, JWT authentication, Redis caching, and clean modular architecture**.

This is a backend API-only application designed for production-ready task and project management.

---

## 🚀 Features

### ✅ Completed

#### Authentication & Authorization
- JWT token-based authentication
- Refresh token mechanism with session management
- Password reset functionality (email-based)
- Email verification with Thymeleaf templates
- Role-based access control (RBAC)
- Permission management system
- Account lockout after failed login attempts
- Secure password hashing (BCrypt)
- **Redis caching** for user authentication (15 min TTL)
- **Cache logging with AOP** for performance monitoring

#### User Management
- User registration with validation
- User profile CRUD operations
- Role assignment (ADMIN, USER, MANAGER, GUEST)
- Permission-based authorization
- User search and filtering
- Password change with validation
- Account enable/disable functionality
- User deletion with cascade handling

#### Projects Module
- Project CRUD operations
- Project member management (OWNER, ADMIN, MEMBER, VIEWER roles)
- Project visibility (PUBLIC/PRIVATE)
- Archive/restore functionality
- Project search and filtering
- Ownership transfer
- Project-level permissions
- Project-level issue counter for unique keys

#### Issues/Tasks Module
- Complete CRUD operations
- **Simplified status workflow**: TODO → IN_PROGRESS → DONE
- **Issue types**: TASK, BUG
- **Priority levels**: LOW, MEDIUM, HIGH
- Issue assignment and tracking
- Due date management
- Soft delete support
- Issue history tracking (audit trail)
- Project-scoped issue keys (e.g., PROJ-123)
- Issue search and filtering

#### Comments Module
- Add comments to issues
- Edit/delete own comments
- Soft delete support with deleted_at timestamp
- User-based comment filtering
- Issue-based comment filtering
- Comment pagination

#### Attachments Module
- File upload to AWS S3
- Support for multiple storage backends (S3, LOCAL)
- MIME type validation
- File size validation
- Attachment metadata storage
- Issue-scoped attachments
- Secure presigned URL generation for S3
- File deletion with cascade handling

### 🚧 Planned
- Advanced search (Full-text with PostgreSQL GIN indexes)
- Notifications system
- Sprint management
- Time tracking
- Custom fields
- Reporting and dashboards

---

## 🏗️ Architecture Decisions

### Issue Status Management
This application uses a **simplified status system** rather than complex workflows:
- **Three statuses**: TODO, IN_PROGRESS, DONE
- **Linear progression**: Issues move sequentially through states
- **Enum-based**: Status stored as VARCHAR with CHECK constraint
- **Rationale**: Keeps the system simple and maintainable for the core use case

This decision avoids over-engineering while still providing effective task tracking. If complex workflows are needed in the future, they can be added through a migration.

### Caching Strategy
- **Redis** for session and user data caching
- **Cache-aside pattern**: Load from cache, fallback to database
- **Automatic eviction**: Cache invalidates on data updates
- **AOP logging**: Track cache hits/misses with Spring AOP

### Storage Strategy
- **AWS S3** for file attachments
- **Presigned URLs** for secure temporary access
- **Fallback to local storage** for development

---

## 📂 Database Migrations

This project uses **Flyway** for database version control.

### First Time Setup

1. Start PostgreSQL and Redis:
```bash
cd docker/postgres
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
   Example: `V15__add_notifications.sql`

2. Write your SQL:
```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
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
- **V6**: ~~Labels~~ (Removed in V11)
- **V7**: ~~Activity Feed~~ (Removed in V14)
- **V8**: Projects Schema Updates
- **V9**: Remove Workflows (Simplified to status enum)
- **V10**: Update Enum Constraints (uppercase STATUS, TYPE, PRIORITY)
- **V11**: Remove Labels (Simplified - redundant with type/priority)
- **V12**: Add Deleted_at to Comments Table
- **V13**: Fix Attachments Storage Type Enum
- **V14**: Remove Activity Feed (Redundant with Issue History)

---

## 🧱 Architecture

This project uses a **package-by-feature** structure:

```
com.pm.taskapp/
├── auth/              # Authentication & authorization
│   ├── controller/    # Auth, User, Role, Permission controllers
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── security/      # JWT, UserPrincipal, Filters
├── project/           # Project management
│   ├── controller/    # Project, ProjectMember controllers
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── enums/
├── task/              # Issues/Tasks (renamed from 'issue')
│   ├── controller/    # Issue, ProjectIssue controllers
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── enums/         # IssueStatus, IssueType, IssuePriority
│   └── mapper/
├── comments/          # Comment system
│   ├── controller/    # IssueComment, UserComment controllers
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
├── attachment/        # File attachments
│   ├── controller/    # Attachment controller
│   ├── service/       # S3 and Local storage services
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── enums/         # StorageType
├── config/            # Application configuration
│   └── cache/         # Redis cache configuration & AOP logging
└── common/            # Shared utilities
    ├── exception/
    └── config/
```

---

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.5.7
- **Java Version**: Java 21
- **Database**: PostgreSQL 15+
- **Cache**: Redis 7.x
- **Storage**: AWS S3 (with local fallback)
- **Security**: Spring Security + JWT (JJWT 0.12.5)
- **ORM**: Spring Data JPA / Hibernate
- **Migration**: Flyway
- **Validation**: Hibernate Validator
- **Email**: Spring Mail + Thymeleaf templates
- **Documentation**: SpringDoc OpenAPI 2.7.0 (Swagger)
- **AOP**: Spring AOP (for cache logging)
- **Build Tool**: Maven
- **Dev Tools**: Spring DevTools, Lombok

---

## 💾 Caching Strategy

The application uses **Redis** for caching to improve performance and reduce database load.

### Cache Configuration

**Location**: `com.pm.taskapp.config.cache.RedisConfig`

- **Serialization**: JSON with Spring Security Jackson modules
- **Default TTL**: 5 minutes
- **Transaction-aware**: Cache operations synchronized with database transactions
- **AOP Logging**: Automatic cache operation logging (debug level)

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

**View cached user**:
```bash
docker exec -it taskapp_redis redis-cli
GET "user:details::<user-id>"
```

**Clear all cache** (development only):
```bash
docker exec taskapp_redis redis-cli FLUSHALL
```

### Cache Logging

Enable DEBUG logging to see cache operations:
```yaml
logging:
  level:
    com.pm.taskapp.config.cache.CacheLoggingAspect: DEBUG
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

OpenAPI JSON specification:
```
http://localhost:8080/v3/api-docs
```

---

## 🔐 Authentication

The API uses JWT (JSON Web Token) for authentication:

1. **Register**: `POST /api/auth/register` - Create new account
2. **Login**: `POST /api/auth/login` - Returns access token and refresh token
3. **Use token**: Include in header: `Authorization: Bearer {token}`
4. **Refresh**: `POST /api/auth/refresh` - Get new access token
5. **Logout**: `POST /api/auth/logout` - Invalidate refresh tokens and clear cache

### Token Configuration
- **Access Token**: 1 hour expiration
- **Refresh Token**: 7 days expiration (stored in database)
- **Cache TTL**: 15 minutes for user details

---

## 🌟 Key Features

### Role-Based Access Control
- **System-wide roles**: ADMIN, USER, MANAGER, GUEST
- **Fine-grained permissions** for different operations
- **Project-level roles**: OWNER, ADMIN, MEMBER, VIEWER
- **Permission inheritance**: Project roles determine access levels

### Project Management
- Public and private projects
- Member management with role-based permissions
- Archive/restore functionality
- Project-level issue counters for unique keys (PROJ-123)
- Ownership transfer with validation

### Issue Tracking (Simplified)
- **Status Flow**: TODO → IN_PROGRESS → DONE
- **Issue Types**: TASK, BUG
- **Priorities**: LOW, MEDIUM, HIGH
- **Issue History**: Complete audit trail of changes
- Soft delete support
- Due date tracking
- Assignment management

### File Attachments
- **AWS S3 integration** with presigned URLs
- **Local storage fallback** for development
- MIME type validation
- File size limits
- Automatic cleanup on issue deletion

### Email System
- Password reset emails with Thymeleaf templates
- Email verification system
- HTML email templates
- SMTP configuration support

---

## 🚀 Getting Started

### Prerequisites
- **Java 21+**
- **PostgreSQL 15+**
- **Redis 7+** (for caching)
- **Maven 3.8+**
- **AWS S3** (optional, for file uploads in production)
- **SMTP Server** (optional, for email features)

### Environment Setup

1. Clone the repository
```bash
git clone <repository-url>
cd taskapp
```

2. Create `.env` file in project root:
```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/taskapp
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Redis
REDIS_PASSWORD=redis_dev_password_change_in_production

# JWT
JWT_SECRET=your_secret_key_min_256_bits
JWT_EXPIRATION=3600000

# AWS S3 (optional)
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_S3_BUCKET_NAME=your_bucket_name
AWS_REGION=us-east-1

# Email (optional)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

3. Start infrastructure services:
```bash
cd docker/postgres
docker-compose up -d
```

4. Run the application:
```bash
mvn spring-boot:run
```

5. Access Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

### Docker Compose Services

The `docker-compose.yml` includes:
- **PostgreSQL** (port 5432)
- **pgAdmin** (port 5050) - Database management UI
- **Redis** (port 6379) - Caching layer

---

## ⚙️ Configuration

### Application Profiles

- **dev**: Development with detailed logging
- **test**: Testing with H2 in-memory database
- **prod**: Production with optimized settings

Activate profile:
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Logging Configuration

```yaml
logging:
  level:
    root: INFO
    com.pm.taskapp: DEBUG
    org.springframework.cache: DEBUG  # Cache operations
    org.flywaydb: INFO
```

---

## 🧪 Testing

Run all tests:
```bash
mvn test
```

Run with coverage:
```bash
mvn clean test jacoco:report
```

---

## ⚠️ Removed Features

### Labels Module (Removed in V11)
- **Reason**: Redundant with existing `type` (TASK/BUG) and `priority` (LOW/MEDIUM/HIGH)
- **Alternative**: Use issue type and priority for categorization
- **Date Removed**: December 2024

### Activity Feed (Removed in V14)
- **Reason**: Redundant with Issue History table
- **Alternative**: Use Issue History for comprehensive audit tracking
- **Date Removed**: December 2024

### Workflow System (Removed in V9)
- **Reason**: Over-engineered for simplified status system
- **Alternative**: Direct status enum (TODO, IN_PROGRESS, DONE)
- **Date Removed**: November 2024

### Audit Trail Strategy
This application uses **Issue History** for comprehensive audit tracking:
- **Issue History**: Tracks all field-level changes (status, assignee, priority, title, description)
- **Location**: `issue_history` table
- **Coverage**: Complete change tracking with old/new values, timestamps, and actor
- **Decision**: Activity Feed removed (V14) as redundant with Issue History

---

## 📧 Contact

For questions or issues, please create a GitHub issue or contact the development team.

---

## 📄 License

[MIT License] - See LICENSE file for details

---

## 🎯 Roadmap

### Upcoming Features
- [ ] Token blacklisting for logout (Redis-based)
- [ ] Advanced search with full-text search
- [ ] Sprint management
- [ ] Time tracking
- [ ] Custom fields
- [ ] Notifications system
- [ ] Reporting dashboards

### Performance Optimizations
- [x] Redis caching for authentication
- [x] Database indexing
- [x] Connection pooling
- [ ] Query optimization
- [ ] Response compression