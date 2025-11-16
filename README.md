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

## 🧱 Architecture

This project uses a **package-by-feature** structure:

