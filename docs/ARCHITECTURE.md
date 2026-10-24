# Architecture

## Components

- **Frontend:** `frontend/index.html`, `frontend/css/styles.css`, and JavaScript modules provide a responsive student directory and dialogs. The app makes fetch requests to the configured REST API; dynamic student values are HTML-escaped when rendering table rows and assigned through `textContent` in the profile dialog.
- **REST API:** `StudentController` maps `/api/students` operations and returns `StudentResponse` DTOs. `StudentRequest` constrains incoming values with Jakarta Validation.
- **Business and persistence:** `StudentService` trims text, normalizes email addresses to lowercase, checks case-insensitive duplicates, and applies CRUD operations through `StudentRepository` / Spring Data JPA.
- **Database:** MySQL stores the `students` table and enforces a unique email constraint. Hibernate uses `validate`; schema setup is done explicitly using `database/schema.sql`.
- **Cross-cutting configuration:** `WebConfig` applies configured CORS origins. `GlobalExceptionHandler` formats validation, malformed request, conflict, missing record, database, and unexpected errors as JSON.

## Request flow

```text
Browser UI
   │ HTTP + JSON (CORS)
   ▼
StudentController ── validates StudentRequest
   │
   ▼
StudentService ── normalize email / enforce business rules
   │
   ▼
StudentRepository ── Spring Data JPA
   │
   ▼
MySQL students table
```

Exceptions from controller/service/database layers are mapped by `GlobalExceptionHandler`; successful read/write responses use the public response DTO. No persistence entity is serialized directly by the API.

## Data and trust boundaries

The API is configured for an explicitly listed set of browser origins. CORS limits which browser origins can read responses; it is not authentication. There is currently no login, authorization, request throttling, or audit trail, so the service is intended for local/demo use without real student data. Keep database credentials on the server side and grant the application user only DML permissions.

## Current design limits

List and search endpoints return all matches; pagination and server-side aggregation are not implemented. Dashboard statistics are calculated in the client from the full list. MySQL schema evolution currently requires a deliberate operator-managed update; Hibernate only validates the schema at runtime.
