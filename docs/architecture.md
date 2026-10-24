# Architecture

The browser frontend talks to the Spring REST API over JSON. CORS is restricted to configured frontend origins. Controllers validate input DTOs and delegate to a service; the service applies business rules and uses a JPA repository; MySQL enforces persistent uniqueness. A global advice maps validation, conflict, missing-record, database, and unexpected errors to a stable JSON error shape.

```text
Browser (HTML/CSS/JS)
        │ JSON / CORS
        ▼
StudentController → StudentService → StudentRepository → MySQL
        │                   │
        └──── GlobalExceptionHandler
```

The frontend derives dashboard statistics from the complete list response. The current setup is intended for a small internship demo; production use should add authentication, schema migrations, observability, and pagination.

