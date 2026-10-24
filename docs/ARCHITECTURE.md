# Architecture

## Components

- **Frontend:** `frontend/index.html`, `frontend/css/styles.css`, and vanilla JavaScript modules provide the responsive directory, sign-in prompt, CRUD forms, filtering/sorting, field feedback, and confirmation dialogs. The page keeps the HTTP Basic authorization header in memory only. User data is HTML-escaped when rendered in rows and assigned through `textContent` in profile/error text.
- **Security boundary:** Spring Security requires HTTP Basic authentication for student API routes. One administrator is provisioned at startup from environment variables, BCrypt-encoded in memory, and granted the `ADMIN` role. The only unauthenticated route is the status-only `GET /actuator/health` probe. This is single-account authentication, not persistent multi-user or granular authorization. Use HTTPS outside local development.
- **REST API:** `StudentController` maps `/api/students` and returns response DTOs. Jakarta Validation constrains incoming `StudentRequest` payloads.
- **Business and persistence:** `StudentService` trims text, normalizes email addresses to lowercase, checks case-insensitive duplicates, and applies CRUD operations through `StudentRepository` / Spring Data JPA.
- **Database:** MySQL is the default runtime and has a unique email constraint. An explicitly selected `local-h2` profile supports isolated local runtime checks. Hibernate uses `validate`; schema creation and updates are operator-managed via `database/schema.sql`.
- **Cross-cutting configuration:** `WebConfig` applies explicitly configured CORS origins. `GlobalExceptionHandler` formats validation, malformed request, conflict, missing record, database, and unexpected errors as JSON.

## Request flow

```text
Browser UI
   │ HTTP Basic + JSON (CORS)
   ▼
Spring Security ── authenticate configured administrator
   │
   ▼
StudentController ── validate StudentRequest
   │
   ▼
StudentService ── normalize email / enforce business rules
   │
   ▼
StudentRepository ── Spring Data JPA
   │
   ▼
MySQL students table (default) or explicitly selected local H2
```

Exceptions from controller/service/database layers are mapped by `GlobalExceptionHandler`; successful read/write responses use public DTOs. Persistence entities are not serialized directly by the API.

## Data and trust boundaries

CORS limits which browser origins can read API responses; it is not authentication. Credentials must be supplied through environment variables and not placed in frontend files or source control. Basic authentication does not encrypt traffic; use HTTPS for any non-local connection. The frontend's sign-out clears its in-memory header; the server has no stateful session or revocation mechanism.

## Current design limits

List and search endpoints return all matches without pagination or server-side sorting. The dashboard filters and sorts the result set in the browser and calculates statistics from the fetched records. The application has no durable user management, roles beyond the single administrator, request throttling, audit history, managed schema migrations, or deployment automation. MySQL connectivity must be verified separately; H2 is not evidence of MySQL integration.
