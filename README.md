# Taskly - Collaborative Task & Workspace Management System

[![CI](https://github.com/Mutabazi-Fab/Taskly-management-system/actions/workflows/ci.yml/badge.svg)](https://github.com/Mutabazi-Fab/Taskly-management-system/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-21-DD0031?logo=angular&logoColor=white)](https://angular.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)

Taskly is a modern, full-stack collaborative Task Management System (TMS) engineered for agile teams and organizations to manage project workflows, track deliverables, balance team workloads, and visualize upcoming deadlines.

Architected with a decoupled Spring Boot REST backend and an Angular reactive single-page client, Taskly is designed both for modular local development and unified single-container deployment (e.g., on Railway, Render, or Docker).

---

## Architecture Overview

```mermaid
graph TD
    Client["Client Browser (Angular 21 SPA)"] -->|REST / JSON with JWT Bearer| Backend["Spring Boot 4.x Backend"]
    Backend -->|Spring Data JPA / Hibernate| DB[(PostgreSQL Database)]

    subgraph "Frontend Layer"
        Client --> AuthG[Route Guards: authGuard, adminGuard]
        Client --> Interceptor[Functional JWT Interceptor]
        Client --> Signals[Reactive Signals State Management]
    end

    subgraph "Backend API Layer"
        Backend --> SecFilter[SecurityFilterChain & JwtAuthenticationFilter]
        Backend --> REST[REST Controllers: Auth, Users, Teams, Boards, Tasks, Notifications]
        Backend --> SPA[SpaRedirectConfig - Single-Container Forwarding]
    end
```

---

## Key Features

### 0. One-Click Live Demo
* **Try the live demo** on the login page logs visitors into a ready-made workspace: a team with teammates, a *Product Launch* board, and ten tasks spread across To Do, In Progress and Done, with overdue, due-today and upcoming deadlines.
* The sample data is created automatically on first start and never duplicated. The shared demo account can't be renamed or deleted by visitors.
* Demo login: `demo@taskly.dev` / `demo1234`. Turn it off with `DEMO_ENABLED=false`.

### 1. Interactive Kanban Workspace
* **Flexible Column Management:** Create, rename, reorder, or delete custom workflow columns.
* **Task Card Management:** Create tasks with rich descriptions, priorities (Low, Medium, High), due dates, and assignees.
* **Drag-and-Drop Workflow:** Move tasks smoothly between workflow stages.
* **Quick Filters & Search:** Filter board cards instantly by priority chips (All, High, Medium, Low) or live title/description search.
* **Batch Column Sweep:** Clear or sweep all completed tasks in a column with one click.
* **One-Click CSV Export:** Export board task data into standard RFC-4180 CSV format for reporting and spreadsheets.

### 2. Smart Due Date Intelligence & Calendar
* **Relative Countdown Badges:** Visual due date indicators (`Today`, `Tomorrow`, `In Xd`, `Overdue`, `Done`) across board and list views.
* **Deadline Calendar:** Dynamic visual schedule cards plotted from real-time database deadlines with status filtering (`All`, `Upcoming`, `Overdue`, `Completed`).

### 3. Analytics & Workload Monitoring
* **Personal KPI Metrics:** Real-time counters for Total Assigned Tasks, In-Progress Tasks, Completed Deliverables, and Completion Rate.
* **Team Workload Roster:** Live progress bars and active task tallies per team member to balance workload distribution.
* **Recent Activity Feed:** Computed human-readable audit log tracking task creation, progress updates, and completions.

### 4. Role-Based Access Control (RBAC) & Administration
* **System Administrators:** Dedicated admin dashboard with live user search, role filter chips (All, Admins, Standard Users), and toggleable administrative privileges.
* **Team Owners:** Authority to manage member rosters and delete owned teams.
* **Standard Members:** Access to team boards, personal task views, and team join requests.

### 5. Team Join Requests & Notification Center
* **Join Requests:** Non-members can request access to existing teams with targeted administrator notifications.
* **Notification Dropdown:** Real-time alert center in the top navigation with:
  * Filter tabs (`Unread` vs. `All`).
  * In-line quick approval or denial with reason prompts.
  * One-click "Mark All as Read" and "Clear Read" controls.

### 6. Authentication & User Customization
* **Secure Auth Experience:** Registration and login with real-time password strength meter, password confirmation validation, show/hide password toggle, and "Remember Me" credentials persistence.
* **Avatar Accent Customization:** Personalized avatar colors persisted in client storage across top headers and profile cards.
* **Default Landing View Preference:** Configure whether Taskly opens to the Dashboard, Kanban Boards, My Tasks, Calendar, or Reports.
* **Design System:** Royal Blue palette (`#3447AA`), glassmorphism card surfaces, custom floating toasts, and confirmation dialogs with no native browser alert popups.

---

## Technology Stack

### Backend
* **Language & Runtime:** Java 21
* **Framework:** Spring Boot 4.x (`spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`)
* **Security:** Spring Security with stateless JWT (JSON Web Token via JJWT `0.11.5`)
* **Validation:** Jakarta Validation API
* **Database Driver & ORM:** PostgreSQL Driver with Hibernate
* **Build Tool:** Apache Maven
* **Containers:** Docker multi-stage build + Docker Compose (app + PostgreSQL 16)

### Frontend
* **Framework:** Angular 21 (Standalone Components, Signals API)
* **Routing & Guards:** Angular Router with `authGuard` and `adminGuard`
* **HTTP:** Angular `HttpClient` with functional `jwtInterceptor`
* **Styling:** Vanilla CSS with design tokens, glassmorphism, and responsive breakpoints
* **Package Manager:** npm

---

## Project Structure

```text
TMS/
├── Dockerfile                      # Builds frontend + backend into one runnable image
├── docker-compose.yml              # Starts the app and PostgreSQL with one command
├── .env.example                    # Optional settings for docker compose (copy to .env)
├── Taskly Backend/                 # Spring Boot REST API
│   ├── src/main/java/com/Task_Management_System/TMS/
│   │   ├── config/                 # SPA redirection and routing configuration
│   │   ├── controller/             # REST controllers (Auth, User, Team, Board, Task, Notifications, etc.)
│   │   ├── dto/                    # Request and response DTO schemas with validation
│   │   ├── exception/              # Global exception handler & custom exceptions
│   │   ├── model/                  # JPA entities (User, Team, TeamMember, Board, TaskList, Task, Notification)
│   │   ├── repository/             # Spring Data JPA repositories
│   │   ├── security/               # JWT token provider, filters, and SecurityFilterChain
│   │   └── service/                # Business logic contracts and implementations
│   └── src/main/resources/
│       ├── application.properties  # Database connection, port binding, and Hibernate configuration
│       └── (static/)               # Not in git: Maven builds the Angular client into the jar
│
└── Taskly Frontend/                # Angular Single Page Application
    ├── src/app/
    │   ├── core/                   # Interceptors, guards, models, and shared HTTP services
    │   ├── features/               # Feature views (login, register, dashboard, admin)
    │   └── app.routes.ts           # Client route definitions
    ├── angular.json                # Angular CLI configuration and asset budgets
    └── package.json                # Frontend dependencies and npm scripts
```

---

## Quick Start with Docker

The fastest way to run Taskly. You only need [Docker Desktop](https://www.docker.com/products/docker-desktop/); Java, Node.js and PostgreSQL are not required.

```bash
docker compose up --build
```

Then open **`http://localhost:8080`** and click **Try the live demo**, or create an account. The first build takes a few minutes; later starts are quick.

* The Angular client is compiled inside the image and served by Spring Boot, so it is always the latest code.
* Data is kept in the `taskly-db` volume and survives restarts. `docker compose down` stops everything; `docker compose down -v` also deletes the data.
* To keep users logged in across restarts, copy `.env.example` to `.env` and set `JWT_SECRET`. Compose reads `.env` automatically, and git ignores it.

---

## Prerequisites

To run Taskly without Docker, make sure you have:
* **JDK 21** installed and configured in your `PATH`
* **Node.js** (v18.x or v20.x+) & **npm**
* **Maven 3.8+** (or use the Maven wrapper if available)
* **PostgreSQL 14+** running locally or a cloud database instance (e.g., Supabase, Neon, Railway)

---

## Environment Variables

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `PORT` | `8080` | Server listening port |
| `DATABASE_HOST` | `localhost` | PostgreSQL host address |
| `DATABASE_PORT` | `5432` | PostgreSQL port |
| `DATABASE_NAME` | `task_mgt_db` | Target PostgreSQL database name |
| `DATABASE_USER` | `postgres` | Database username |
| `DATABASE_PASSWORD` | `123` | Database user password |
| `JWT_SECRET` | *(random per startup)* | Secret used to sign login tokens, at least 32 characters. **Required in production**: without it, a random key is generated and everyone is logged out on each restart |
| `JWT_EXPIRATION_MS` | `86400000` | Token lifetime in milliseconds (default 24 hours) |
| `DEMO_ENABLED` | `true` | Creates the sample demo workspace on first start and shows **Try the live demo** on the login page |

Generate a strong secret with `openssl rand -base64 48` (or PowerShell: `[Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))`).

---

## Getting Started Locally

### 1. Database Setup
Create a PostgreSQL database:
```sql
CREATE DATABASE task_mgt_db;
```

Update your connection credentials in `Taskly Backend/src/main/resources/application.properties` (or set the environment variables listed above):
```properties
spring.datasource.url=jdbc:postgresql://${DATABASE_HOST:localhost}:${DATABASE_PORT:5432}/${DATABASE_NAME:task_mgt_db}
spring.datasource.username=${DATABASE_USER:postgres}
spring.datasource.password=${DATABASE_PASSWORD:your_password}
```

### 2. Run the Backend API
From the repository root:
```bash
cd "Taskly Backend"
mvn spring-boot:run -DskipFrontend=true
```
The REST API will start on **`http://localhost:8080`**. `-DskipFrontend=true` skips bundling the Angular client, since in development it is served by `ng serve` (next step).

### 3. Run the Frontend Client
In a separate terminal window:
```bash
cd "Taskly Frontend"
npm install
npx ng serve
```
Access the client application at **`http://localhost:4200`**.

---

## Running the Tests

**Backend** (JUnit 5, Mockito, Spring MockMvc):
```bash
cd "Taskly Backend"
mvn test -DskipFrontend=true
```
Tests tagged `database` (the full application startup test and the user-deletion integration test) run against PostgreSQL, so it needs to be running. The user-deletion test rolls back its data when it finishes. To run only the fast tests that need no database:
```bash
mvn test -DskipFrontend=true -DexcludedGroups=database
```
Without `-DskipFrontend=true`, Maven also builds the Angular client first (see below).

**Frontend** (Vitest via the Angular CLI):
```bash
cd "Taskly Frontend"
npx ng test --watch=false
```

### Continuous Integration
Every push and pull request to `main` runs [GitHub Actions](.github/workflows/ci.yml) with three parallel jobs:
* **Backend:** `mvn verify` on JDK 21 against a throwaway PostgreSQL 16 container.
* **Frontend:** `npm ci`, the unit tests, and a production build on Node.js 24.
* **Docker:** builds the full image, so a broken `Dockerfile` is caught before anyone tries to run it.

---

## Monolithic Production Build & Deployment

Taskly can be packaged into a single deployable Spring Boot JAR containing the compiled Angular frontend. This eliminates CORS overhead in production and allows single-service hosting on platforms like **Railway**.

### Build the JAR
One command builds both halves:
```bash
cd "Taskly Backend"
mvn clean package -DskipTests
```
During the build, Maven (via `frontend-maven-plugin`):
1. Downloads a pinned Node.js into `Taskly Frontend/node/` (first build only; no global Node.js needed).
2. Runs `npm ci` and the production `ng build` in `Taskly Frontend`.
3. Copies the result into the JAR's `static/` folder.

The compiled bundle is never committed, so the JAR always contains the current frontend code. The resulting JAR in `target/TMS-0.0.1-SNAPSHOT.jar` serves both the REST endpoints under `/api/**` and the Angular client for all other web routes via `SpaRedirectConfig`. Pass `-DskipFrontend=true` for a faster backend-only build.

### Deploying to Cloud Providers (e.g, Railway)
1. Link your GitHub repository to Railway.
2. Provide the required environment variables:
   * `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_NAME`, `DATABASE_USER`, `DATABASE_PASSWORD` (or `DATABASE_URL`)
   * `JWT_SECRET` (a long random string; see [Environment Variables](#environment-variables))
   * `PORT` (assigned automatically by the platform)
3. The server will bind to the cloud environment port and serve the full workspace under a single secure domain.

---

## REST API Overview

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Authenticate user credentials and receive JWT | Public |
| `POST` | `/api/users` | Register a new user account | Public |
| `GET` | `/api/users` | Retrieve all registered users | Authenticated |
| `PATCH` | `/api/users/{id}/admin` | Promote or demote user administrator status | Admin Only |
| `GET` | `/api/teams` | List all teams | Authenticated |
| `POST` | `/api/teams` | Create a new team workspace | Authenticated |
| `DELETE`| `/api/teams/{id}` | Delete a team | Owner / Admin |
| `GET` | `/api/boards/{boardId}/lists` | Retrieve all column lists for a board | Authenticated |
| `POST` | `/api/lists/{listId}/tasks` | Create a task within a column | Authenticated |
| `PATCH` | `/api/tasks/{id}/status` | Update task workflow status | Authenticated |
| `DELETE`| `/api/tasks/{id}` | Delete task | Authenticated |
| `GET` | `/api/notifications` | Fetch notifications for current user | Authenticated |
| `PATCH` | `/api/notifications/read-all`| Mark all notifications as read | Authenticated |
| `DELETE`| `/api/notifications/clear-read`| Clear read notifications | Authenticated |
| `POST` | `/api/join-requests` | Submit request to join a team | Authenticated |
| `POST` | `/api/join-requests/{id}/approve` | Approve a pending join request | Admin |
| `POST` | `/api/join-requests/{id}/deny` | Deny a pending join request with reason | Admin |
