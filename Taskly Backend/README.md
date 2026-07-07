# Taskly — Task Management System Backend

Taskly is a robust, production-ready task management and productivity API built with **Spring Boot** (Java 21) and **PostgreSQL**. The backend utilizes a clean, layered architecture (`controller / service / repository / model`) to manage users, teams, boards, columns, tasks, and file attachments securely under JWT authentication.

---

## 🚀 Key Features

* **User Management & Auth:** Secured registration and login endpoints utilizing **Spring Security** and **BCrypt** password hashing with stateless **JWT tokens**.
* **Team Collaboration:** Create teams, assign users as members or owners, and validate membership constraints.
* **Agile Board Framework:** Nest boards under teams, and organize columns (task lists) inside boards using custom layouts/order positioning.
* **Task Lifecycle Management:** Create tasks with LOW/MEDIUM/HIGH priorities, dueDate constraints, assignments, and handle status transitions (`TODO` -> `IN_PROGRESS` -> `DONE`).
* **Progress Aggregation:** Retrieve board-level task counts grouped by status.
* **Database Cascading & Integrity:** 
  * User deletion automatically unassigns tasks (assignee set to `NULL`) instead of deleting them.
  * Deleting a team automatically cascades to delete nested memberships, boards, lists, tasks, and attachments.

---

## 🛠️ Technology Stack

* **Language:** Java 21
* **Backend Framework:** Spring Boot 3+ (Data JPA, Web, Security, Validation)
* **Database:** PostgreSQL
* **Security:** JWT (JSON Web Tokens) with HS256 encryption
* **Build Tool:** Maven

---

## ⚙️ Configuration & Database Setup

1. Make sure you have a local PostgreSQL instance running.
2. Create a database named `task_mgt_db`.
3. Configure the database credentials in `src/main/resources/application.properties`:

```properties
spring.application.name=final_backend
spring.datasource.url=jdbc:postgresql://localhost:5432/task_mgt_db
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

## 🏃 How to Run the Project

Navigate to the project root and run the Spring Boot server using the Maven wrapper:

```bash
# Windows PowerShell
.\mvnw spring-boot:run

# Bash (Linux/macOS)
./mvnw spring-boot:run
```

The application will launch on `http://localhost:8080`.

---

## 📮 Postman E2E Testing Flow

All endpoints (except registration and login) require a Bearer token in the `Authorization` header: `Authorization: Bearer <your_jwt_token>`.

### Step 1: Register User
* **Method:** `POST`
* **URL:** `/api/users`
* **Request Body:**
  ```json
  {
    "username": "fabrice",
    "email": "fabrice@example.com",
    "password": "mySecurePassword"
  }
  ```
* **Expected Response:** `201 Created` (returns generated `id`).

### Step 2: User Login
* **Method:** `POST`
* **URL:** `/api/auth/login`
* **Request Body:**
  ```json
  {
    "email": "fabrice@example.com",
    "password": "mySecurePassword"
  }
  ```
* **Expected Response:** `200 OK` (returns JSON containing a `"token"` string. Copy this token for future requests).

### Step 3: Create Team
* **Method:** `POST`
* **URL:** `/api/teams`
* **Request Body:**
  ```json
  {
    "name": "Engineering Team",
    "ownerId": "INSERT_USER_UUID_FROM_STEP_1"
  }
  ```
* **Expected Response:** `201 Created` (returns team `id`).

### Step 4: Create Board under Team
* **Method:** `POST`
* **URL:** `/api/teams/INSERT_TEAM_UUID/boards`
* **Request Body:**
  ```json
  {
    "name": "Sprint 1 Kanban"
  }
  ```
* **Expected Response:** `201 Created` (returns board `id`).

### Step 5: Create Task List (Column) under Board
* **Method:** `POST`
* **URL:** `/api/boards/INSERT_BOARD_UUID/lists`
* **Request Body:**
  ```json
  {
    "name": "To Do",
    "position": 0
  }
  ```
* **Expected Response:** `201 Created` (returns list `id`).

### Step 6: Create Task inside List
* **Method:** `POST`
* **URL:** `/api/lists/INSERT_LIST_UUID/tasks`
* **Request Body:**
  ```json
  {
    "title": "Configure Spring Security",
    "description": "Restrict access to endpoints using JWT filter",
    "priority": "HIGH",
    "dueDate": "2026-07-15",
    "creatorId": "INSERT_USER_UUID",
    "assigneeId": "INSERT_USER_UUID"
  }
  ```
* **Expected Response:** `201 Created` (returns task `id`).

### Step 7: Update Task Status
* **Method:** `PATCH`
* **URL:** `/api/tasks/INSERT_TASK_UUID/status`
* **Request Body:**
  ```json
  {
    "status": "IN_PROGRESS"
  }
  ```
* **Expected Response:** `200 OK` (verifies task status transitioned).

### Step 8: Get Board Aggregated Progress Stats
* **Method:** `GET`
* **URL:** `/api/boards/INSERT_BOARD_UUID/stats`
* **Expected Response:** `200 OK` (returns counts of tasks by status type).
  ```json
  {
    "todoCount": 0,
    "inProgressCount": 1,
    "doneCount": 0
  }
  ```
