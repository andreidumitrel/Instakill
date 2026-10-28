# Instakill

Instakill is a Spring Boot 3 / Vue 3 starter for an Instagram-style social feed. The project starts as a monolith but is structured for future extraction into independent services.

## Project layout

- `backend/` – Spring Boot application exposing REST and MVC endpoints, backed by SQLite.
- `frontend/` – Vue 3 SPA that consumes the REST API.
- `scripts/` – helper tooling including Gradle bootstrap.

## Quick Start

### Prerequisites
- **Java**: Java 21+ (e.g. OpenJDK 21 LTS).
- **Node.js** *(Optional)*: Required only if running the Vue 3 SPA frontend (download from [nodejs.org](https://nodejs.org/)).

---

### 1. Running the Backend (Spring Boot)

The backend provides both the REST API and a built-in Thymeleaf web interface (no Node.js required).

#### First time setup (Windows PowerShell):
If the Gradle wrapper is not yet generated, run the bootstrap script:
```powershell
./scripts/bootstrap-gradle.ps1
```

#### Starting the server:
Make sure `JAVA_HOME` points to your JDK 21 installation, then run `bootRun`:
```powershell
# Set JAVA_HOME if not already in system environment
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-21.0.2"

cd backend
.\gradlew.bat bootRun
```

On Linux / macOS:
```bash
./scripts/bootstrap-gradle.sh
cd backend
./gradlew bootRun
```

#### Useful endpoints:
- **Web feed**: http://localhost:8080/feed
- **Login page**: http://localhost:8080/auth/login
- **REST OpenAPI UI**: http://localhost:8080/api/swagger
- **Health check**: http://localhost:8080/actuator/health

---

### 2. Running the Frontend (Vue 3 SPA)

The Vue SPA is located in `frontend/` and connects to the backend at `http://localhost:8080`.

1. Ensure **Node.js LTS** and **npm** are installed.
2. In a separate terminal:
```powershell
cd frontend
npm install
npm run dev
```
3. Open http://localhost:5173/ in your browser.

---

## Authentication

Authentication uses JWT stored in an `ACCESS_TOKEN` HttpOnly cookie. REST clients can authenticate by capturing the cookie returned from `/api/auth/login` or by sending the token manually.

Seed accounts (development profile):

| Username | Email             | Password |
|----------|-------------------|----------|
| alice    | alice@example.com | password |
| bob      | bob@example.com   | password |

---

## Running tests

```powershell
cd backend
.\gradlew.bat test
```
(On Linux / macOS: `./gradlew test`)

The tests cover feed ranking, like toggling, MVC flows, and an end-to-end REST happy-path.

