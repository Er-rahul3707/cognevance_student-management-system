# Cognevance Student Management System

A Java full-stack student directory for maintaining enrollment records through a responsive browser dashboard and a Spring Boot REST API. MySQL 8 is the default runtime database. An explicit local H2 profile is available for isolated development and runtime checks when MySQL administrator setup is unavailable; it does not alter the default MySQL configuration.

## Features

- Create, view, update, delete, and search student records by name, email, course, or numeric ID.
- Server-side validation, case-insensitive duplicate email protection, useful field errors, and appropriate HTTP status codes.
- Responsive dashboard with loading, empty, success, and error states, student details, and confirmation before deletion.
- Environment-based database and CORS configuration; no credentials are required in source files.
- HTTP Basic administrator authentication for every backend route, with BCrypt-encoded in-memory credentials from environment variables and a browser sign-in form.

The API requires the configured single administrator account. The credential is loaded from environment variables and never persisted as an application user. This is suitable for a controlled local demo; use HTTPS, managed identity lifecycle, rate limiting, and a durable user store before a public deployment.

## Technology and architecture

| Area | Stack |
| --- | --- |
| Backend | Java 17, Spring Boot 3.5.16, Spring Web, Spring Data JPA, Jakarta Validation |
| Runtime database | MySQL 8 (default); opt-in file-backed H2 profile for local development |
| Frontend | HTML, CSS, vanilla JavaScript modules |
| Verification | Maven, JUnit 5, Spring MockMvc, Node.js built-in test runner |

The browser calls the REST controller. DTO validation and service rules are applied before JPA persistence. Responses use DTOs, and centralized exception handling returns consistent JSON errors. See [architecture](docs/ARCHITECTURE.md), [setup](docs/SETUP.md), and [API reference](docs/API.md).

## Project structure

```text
backend/       Spring Boot application, configuration, and H2-backed tests
  src/main/    API, domain, persistence, and error handling
  src/test/    automated backend tests
  data/        ignored local-H2 files, created only by the local-h2 profile
 database/     MySQL schema and least-privilege application user setup
 docs/         architecture, API, setup, and verification notes
 frontend/     static dashboard, styles, and utility tests
.env.example  safe local variable template
```

## Prerequisites

- JDK 17 or newer, Maven 3.6 or newer.
- MySQL 8.x for the default runtime configuration, or use the opt-in H2 profile for local development.
- Python 3 (or another static HTTP server) to serve the frontend.
- Node.js 18 or newer for frontend checks.

## Run locally

For MySQL, follow [docs/SETUP.md](docs/SETUP.md) to initialize the project database and dedicated app user. Spring Boot does not load `.env` files automatically; set environment variables in the same PowerShell terminal used to launch Maven. Hibernate validates the MySQL schema and does not modify it.

Set the administrator credentials in the same PowerShell terminal before starting either profile. Use a unique username and a randomly generated password of at least 16 Unicode characters and no more than 72 UTF-8 bytes. The password is held in that terminal's process environment; do not put a real value in a script or commit it.

```powershell
$env:APP_AUTH_USERNAME = 'admin'
$securePassword = Read-Host 'Enter a unique local password (16+ characters)' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try { $env:APP_AUTH_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer) }
```

For a local isolated H2 runtime, run from the repository root:

```powershell
mvn -f backend/pom.xml -Plocal-h2 spring-boot:run '-Dspring-boot.run.profiles=local-h2'
```

This profile uses a persistent file database under ignored `backend/data/`. It is opt-in and does not replace or modify the MySQL defaults. To run against MySQL, omit `-Plocal-h2` and the profile argument.

In another PowerShell terminal, serve the frontend:

```powershell
python -m http.server 5500 --directory frontend
```

Open [http://localhost:5500](http://localhost:5500). The default API base URL is `http://localhost:8080/api`. For a different API origin, configure `window.STUDENT_API_BASE_URL` in `frontend/index.html` and allow that exact frontend origin with `CORS_ALLOWED_ORIGINS`. Never put database credentials in frontend files.

## API

Base URL: `http://localhost:8080/api`. See [docs/API.md](docs/API.md) for fields, examples, and error format.

| Method | Endpoint | Purpose | Success |
| --- | --- | --- | --- |
| GET | `/students` | List students | 200 |
| GET | `/students?q=asha` | Search records | 200 |
| GET | `/students/{id}` | Retrieve one record | 200 |
| POST | `/students` | Create a record | 201 |
| PUT | `/students/{id}` | Update a record | 200 |
| DELETE | `/students/{id}` | Delete a record | 204 |

## Tests and current verification

Run backend and frontend checks from the repository root:

```powershell
mvn -f backend/pom.xml test
mvn -f backend/pom.xml package
node --check frontend/js/app.js
node --check frontend/js/student-utils.mjs
node --test frontend/js/student-utils.test.mjs
```

See [docs/TESTING.md](docs/TESTING.md) for actual results and the distinction between automated tests, live H2 API checks, browser checks, and MySQL integration status.

**Verification on 2026-10-25:** backend tests passed 13/13, Maven package passed, and frontend utility tests passed 5/5. A live Spring Boot API workflow passed against the explicitly selected file-backed H2 profile, including anonymous 401, authenticated CRUD, search, invalid-input 400, delete 204, missing-after-delete 404, and cleanup. The browser displayed the new administrator sign-in dialog, but authenticated browser CRUD was not rerun because the available browser automation must not enter credentials into an authentication form. An earlier pre-auth browser CRUD run is documented in [TESTING.md](docs/TESTING.md) and does not verify the current sign-in flow. MySQL 8 is installed/running, but the project app account receives MySQL error 1045 and administrator credentials were unavailable; therefore real MySQL connection/CRUD remain unverified. Do not treat H2 checks as MySQL verification.

## Troubleshooting

- **MySQL schema validation failure:** use an authorized administrator to run `database/schema.sql` on the intended `student_management` schema.
- **MySQL error 1045:** verify credentials and account host grants locally. Do not reuse another project's database credentials. See [docs/SETUP.md](docs/SETUP.md).
- **Local H2 startup:** include both `-Plocal-h2` and `'-Dspring-boot.run.profiles=local-h2'`; generated files are stored under ignored `backend/data/`.
- **Frontend cannot reach API:** ensure the backend is listening on 8080 and `CORS_ALLOWED_ORIGINS` exactly matches the frontend origin.
- **Port conflict:** set `SERVER_PORT` or use another static-server port, then update CORS.
- **No npm on PATH:** the direct Node commands above run the existing utility tests without npm.

## Security and limitations

`.env` and `backend/data/` are ignored by Git; `.env.example` contains placeholders only. The backend refuses to start unless `APP_AUTH_USERNAME` is nonblank and `APP_AUTH_PASSWORD` contains at least 16 Unicode characters and no more than 72 UTF-8 bytes. Basic authentication must be used only over HTTPS outside localhost. Credentials are BCrypt-encoded in memory on startup, and the browser keeps its Basic authorization value in page memory only (refresh signs out). Use a dedicated least-privilege database account. Avoid real student data in tests, source control, or screenshots. CORS is not authentication. The API currently returns all matching rows without pagination or sorting and has no durable user management, authorization roles beyond administrator, audit history, managed migrations, or deployment automation. These are remaining production limitations.

## Deployment preparation

The application can be prepared for a demo host by serving the static frontend separately and running the Spring Boot jar with a persistent remote MySQL database. Set `APP_AUTH_USERNAME`, `APP_AUTH_PASSWORD`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`, and the host-provided `PORT` (or `SERVER_PORT`) as environment variables; use HTTPS and MySQL Connector/J `sslMode=VERIFY_IDENTITY` for remote database connections. Do not use the local H2 file profile on ephemeral hosts. This repository has no deployment configuration and has not been deployed. The current single-account Basic authentication is not a complete multi-user production identity system; free tiers are demonstration infrastructure, not production availability guarantees.

## Screenshots

No screenshots are committed. The local browser verification used synthetic test data and was not captured as a project screenshot.

## License and author

See the [MIT License](LICENSE).
