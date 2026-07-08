# Taskly - Project Management & Collaboration Workspace

Taskly is a robust, full-stack collaborative Task Management System (TMS) designed for organizations and teams to track tasks, organize columns, review deadlines, and manage user workloads. 

Built as a decoupled architecture that packages into a unified Spring Boot monorepo for seamless single-instance hosting (e.g., on Railway).

---

## 🛠️ Technology Stack
* **Backend:** Java 21, Spring Boot 3.x, Spring Security (JWT Stateless Authentication), Spring Data JPA.
* **Database:** PostgreSQL.
* **Frontend:** Angular 18+, Reactive signals for state management, Vanilla CSS (Premium glassmorphic styling, HSL colors).

---

## ✨ Features
1. **Interactive Kanban Board:** Add, rename, or delete columns, create tasks, assign members, filter by priority or assignee, and drag/move tasks across columns.
2. **Deadline Calendar:** Dynamic visual schedule plotting calendar cards computed from real-time database deadlines.
3. **Role-Based Permissions:** 
   * **System Administrators:** Can manage registered users, toggle admin privileges, and create teams.
   * **Team Owners:** Can add/remove members and delete their team.
   * **Standard Users:** Can view personal tasks, request to join teams, and work on boards they are member of.
4. **Team Join Requests:** Standard users can request to join any team. Requests target a selected administrator, updating their in-app notification center.
5. **In-App Notification Center:** A custom top-right bell dropdown displaying real-time unread alerts.
   * Admins can approve or deny requests directly from the dropdown. Rejections prompt a reason textbox.
   * Notifications self-heal and disappear immediately once processed.
6. **Polished In-App UI Popups:** Uses fully responsive floating toast notifications and center-aligned modal confirmation boxes, completely avoiding default grey browser dialogs.
7. **Workspace Analytics:** Computed workload statistics card showing active tasks and progress bars per team member.

---

## 📂 Project Structure
```text
TMS/
├── Taskly Backend/                 # Spring Boot REST API
│   ├── src/main/java/.../
│   │   ├── config/                 # SPA Redirection & Security Filters
│   │   ├── controller/             # REST Endpoints (Auth, Boards, Teams, Tasks, Notifications)
│   │   ├── dto/                    # Request/Response Data Transfer Objects
│   │   ├── exception/              # Global REST Exception Handlers
│   │   ├── model/                  # Database Entities (Cascade Deletions configured)
│   │   ├── repository/             # Database Query Interfaces
│   │   └── service/                # Business Logic Implementations
│   └── src/main/resources/
│       ├── application.properties  # Database credentials & Railway port bindings
│       └── static/                 # Bundled Angular frontend production assets
│
└── Taskly Frontend/                # Angular Single Page Application (SPA)
    ├── src/app/
    │   ├── core/                   # JWT Interceptors, Auth/Admin Guards, HTTP Services, Models
    │   ├── features/               # Components (Login, Register, Dashboard, Admin panels)
    │   └── main.ts                 # App Bootstrapping
    ├── angular.json                # Increased budgets (30kb/50kb styles limits)
    └── package.json                # Project dependencies
```

---

## 📋 Prerequisites
Ensure you have the following installed on your local machine:
* **JDK 17 or higher** (JDK 21 recommended)
* **Node.js** (v18.x or higher) & **npm** (v9.x or higher)
* **Maven 3.8+**
* **PostgreSQL** instance running locally or hosted online.

---

## 🚀 How to Run Locally

### 1. Database Setup
Create a PostgreSQL database named `taskly`:
```sql
CREATE DATABASE taskly;
```
Configure your credentials in `Taskly Backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/taskly
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 2. Run the Backend
Open a terminal in the `Taskly Backend` folder and run:
```bash
mvn spring-boot:run
```
The server will boot up locally on **`http://localhost:8080`**.

### 3. Run the Frontend
Open a new terminal in the `Taskly Frontend` folder and run:
```bash
npm install
npx ng serve
```
The client dashboard will compile and be available at **`http://localhost:4200`**.

---

## 🚂 Packaging & Deployment (Railway)
Taskly is configured to deploy both the frontend and backend in a single container.

### 1. Compile the Frontend
Build the optimized production frontend bundle:
```bash
cd "Taskly Frontend"
npx ng build --configuration production
```
This compiles the SPA assets inside `dist/taskly-frontend/browser`.

### 2. Bundle into Backend
Copy the static assets into the Spring Boot resource static directory:
* **Windows (PowerShell):**
  ```powershell
  Remove-Item -Recurse -Force "..\Taskly Backend\src\main\resources\static\*"; Copy-Item -Recurse -Force "dist\taskly-frontend\browser\*" "..\Taskly Backend\src\main\resources\static\"
  ```
* **macOS/Linux:**
  ```bash
  rm -rf "../Taskly Backend/src/main/resources/static/*" && cp -r dist/taskly-frontend/browser/* "../Taskly Backend/src/main/resources/static/"
  ```

### 3. Deploy
Commit your changes and push the project to GitHub. Link the repository to your Railway project. 
* Railway will read the parent Maven configuration, map the dynamic database connection URL (`JDBC_DATABASE_URL`), bind the internal port (`PORT`), and spin up the complete application under a single URL!
