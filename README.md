# Cognevance Student Management System

A Java full-stack student directory for creating, searching, viewing, updating, and deleting enrollment records. The browser dashboard uses a Spring Boot REST API and MySQL database. The project is designed as a small internship submission; it does not include authentication or production deployment.

## Table of contents

- [Overview](#overview)
- [Features](#features)
- [Technology](#technology)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Local setup and run](#local-setup-and-run)
- [API reference](#api-reference)
- [Testing](#testing)
- [Demonstration](#demonstration)
- [Troubleshooting](#troubleshooting)
- [Security notes](#security-notes)
- [Limitations and future work](#limitations-and-future-work)
- [Contributing](#contributing)
- [License](#license)
- [Author](#author)

## Overview

The application provides a simple interface to maintain student enrollment details in one persistent directory. A user can search records by name, email, course, or numeric ID, see enrollment statistics, and manage each record through the API-backed UI.

## Features

- Create, view, edit, and delete student records.
- Search across student name, email, course, and numeric student ID.
- Validate required fields, email, phone format, and enrollment date.
- Prevent duplicate email addresses case-insensitively and store emails in lowercase.
- Responsive dashboard with totals, distinct course count, and this-month enrollment count.
- Loading and empty states, accessible labels, focused dialogs, confirmation on delete, and feedback toasts.
- JSON API errors with field-level validation messages and generic server errors.
- Environment-based backend configuration and explicit CORS origins.

Pagination, sorting, authentication, and role-based access are not implemented.

## Technology

| Area | Stack |
| --- | --- |
| Backend | Java 17, Spring Boot 3.3.5, Spring Web, Spring Data JPA, Jakarta Validation |
| Persistence | MySQL 8; H2 only for automated tests |
| Frontend | HTML, CSS, vanilla JavaScript modules |
| Build and tests | Maven, JUnit 5, Spring MockMvc, Node.js built-in test runner |

## Architecture

The browser sends JSON requests to the Spring MVC controller. Request DTO validation and service rules run before the repository persists entities with JPA. Responses use a dedicated DTO; shared exception handling returns a consistent error object. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Project structure

```text
backend/       Spring Boot application, configuration, and integration tests
  src/main/    API, domain, persistence, and shared errors
  src/test/    H2-backed API integration tests
database/      MySQL database, user, and table bootstrap SQL
docs/          Architecture, API, setup, and test documentation
frontend/      Static dashboard, styles, and frontend utility tests
.env.example   Local environment variable template
```

## Prerequisites

- JDK 17 or newer.
- Maven 3.6 or newer.
- MySQL 8.x for the application runtime.
- Python 3 (or another static HTTP server) to serve the frontend.
- Node.js 18 or newer to run the frontend checks.

## Local setup and run

Follow [docs/SETUP.md](docs/SETUP.md) to create the MySQL database and application user. The SQL script contains a clearly marked placeholder password; replace it locally before execution. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `CORS_ALLOWED_ORIGINS` in the backend terminal. Spring Boot does not load `.env` files automatically. Hibernate validates the existing schema at startup; it does not create or alter tables.

Start the backend from the repository root:

```powershell
mvn -f backend/pom.xml spring-boot:run
```

In a second terminal, serve the frontend:

```powershell
python -m http.server 5500 --directory frontend
```

Open [http://localhost:5500](http://localhost:5500). The default API URL is `http://localhost:8080/api`. For another API host, set `window.STUDENT_API_BASE_URL` in `frontend/index.html` before the module loads and set the backend `CORS_ALLOWED_ORIGINS` to the frontend's exact origin. Never put database credentials in frontend code.

## API reference

The base URL is `http://localhost:8080/api`. Dates use ISO `YYYY-MM-DD`. Full request/response details and examples are in [docs/API.md](docs/API.md).

| Method | Endpoint | Purpose | Success |
| --- | --- | --- | --- |
| GET | `/students` | List students, newest ID first | 200 |
| GET | `/students?q=asha` | Search name, email, course, or ID | 200 |
| GET | `/students/{id}` | Read one student | 200 |
| POST | `/students` | Create a student | 201 |
| PUT | `/students/{id}` | Update a student | 200 |
| DELETE | `/students/{id}` | Delete a student | 204 |

Example create request:

```powershell
$body = @{ fullName = 'Asha Patel'; email = 'asha@example.com'; phone = '+91 98765 43210'; course = 'Computer Science'; enrollmentDate = '2025-01-10' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/students' -ContentType 'application/json' -Body $body
```

The server generates `id` and `createdAt`. A duplicate email returns 409, invalid input returns 400, an unknown ID returns 404, and database access errors return a generic 503.

## Testing

Run these commands from the repository root:

```powershell
mvn -f backend/pom.xml test
mvn -f backend/pom.xml package
npm --prefix frontend run check
npm --prefix frontend test
```

If `npm` is unavailable but Node.js is installed, run the frontend checks directly:

```powershell
node --check frontend/js/app.js
node --check frontend/js/student-utils.mjs
node --test frontend/js/student-utils.test.mjs
```

The backend test profile uses an in-memory H2 database. It verifies API behavior but does not prove MySQL connectivity or MySQL-specific compatibility. See [docs/TESTING.md](docs/TESTING.md) for the latest actual results and environment limits.

**Latest verification (2026-10-24):** `mvn -f backend/pom.xml test` passed (6/6); frontend JavaScript syntax checks and utility tests passed (5/5). MySQL 8 is installed and running locally, but the dedicated app-user login was rejected with MySQL error 1045 and no administrator login was available. The backend could not start against the project's isolated database. The frontend rendered in the browser, but the API request failed; no live CRUD or MySQL integration is claimed. See [docs/TESTING.md](docs/TESTING.md) for details and the required local setup action.

## Demonstration

Once MySQL credentials are configured and the API starts, the dashboard should display the current directory. Add a student, search by each supported field, open its details, edit it, try a duplicate email, then delete it with the confirmation prompt. In the 2026-10-24 runtime check, the frontend loaded but could not reach the API because the local database login was unavailable; no live CRUD session or runtime screenshot is claimed.

## Troubleshooting

- **API fails to start with a schema validation error:** Run `database/schema.sql` against the intended `student_management` MySQL database and confirm it contains the `students` table.
- **Access denied:** Check local `DB_USERNAME` and `DB_PASSWORD` values and the MySQL user's host grant. Do not use unrelated database credentials.
- **MySQL error 1045 for `student_app`:** Use a local MySQL administrator in Workbench or the CLI to run `database/schema.sql` after replacing its password placeholder, then set matching `DB_USERNAME` and `DB_PASSWORD` variables for the application. `CREATE USER IF NOT EXISTS` does not change the password of an account that already exists; reset only this project's dedicated account or choose another dedicated account and grant it DML access to `student_management`.
- **Frontend cannot reach the API:** Confirm the API is listening on port 8080 and that `CORS_ALLOWED_ORIGINS` exactly matches the frontend origin, including port.
- **Port already in use:** Change `SERVER_PORT` for the API or serve the frontend on another port and update CORS accordingly.
- **`npm` command is unavailable:** Install Node.js 18+ (which includes npm), or if `node` is available run the direct checks in the [testing section](#testing).

## Security notes

- `.env` is ignored by Git; `.env.example` contains only a placeholder. Set real credentials through local environment variables or a secret manager.
- The application account should have only the permissions needed to read and modify records. The provided SQL script creates the schema/table as an administrator; its app user receives DML grants only.
- Do not place real student records in source control or screenshots.
- CORS is not authentication. The application has no user login or authorization and should not be exposed publicly with real student data as-is.
- Use TLS, network restrictions, backups, managed migrations, and access control before any production use.

## Limitations and future work

The API returns the full matching result set with no pagination or sorting. Dashboard statistics are computed client-side from the fetched full list. Authentication, authorization, audit history, migrations, and deployment automation are not included. Those are potential next steps if the project scope expands.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for the local workflow and verification expectations.

## License

The project includes an [MIT License](LICENSE).

## Author

**Rahul Chandra Sharma**
