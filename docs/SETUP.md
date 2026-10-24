# Local setup

This guide runs the existing Spring Boot API and static frontend on Windows. MySQL 8 remains the default runtime database. The optional H2 profile is isolated and must be selected explicitly.

## Prerequisites

- JDK 17+ and Maven 3.6+.
- MySQL 8.x and an authorized local administrator for MySQL setup (unless using the optional H2 profile).
- Python 3 or another static HTTP server.
- Node.js 18+ for frontend checks.

## MySQL database and application account

From the repository root, have an authorized MySQL administrator execute `database/schema.sql` in Workbench or the CLI. Replace the SQL file's clearly marked password placeholder with a strong local password before execution. The script creates the `student_management` schema and table and grants the dedicated `student_app` account only SELECT, INSERT, UPDATE, and DELETE. The default Spring profile validates the schema at startup; it does not create or migrate tables.

PowerShell command using the standard MySQL 8 installation path:

```powershell
cmd.exe /c '"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < database\schema.sql'
```

Alternatively, execute the file in MySQL Workbench. If `student_app` already exists, `CREATE USER IF NOT EXISTS` does not change its password. An administrator must reset only this project's dedicated account or use another dedicated account with the same limited grants. Do not use credentials from another project.

## Configure and run with MySQL

Set variables in the same PowerShell terminal where Maven runs (use the local password selected for the project account):

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/student_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:DB_USERNAME = 'student_app'
$env:DB_PASSWORD = 'your-local-password'
$env:SERVER_PORT = '8080'
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5500,http://127.0.0.1:5500'
mvn -f backend/pom.xml spring-boot:run
```

Spring Boot does not load `.env` automatically. `.env.example` is a reference only. Do not commit real credentials. For non-local MySQL, configure the host grant and JDBC URL deliberately.

## Optional isolated local H2 runtime

Use this only for local development/API and browser verification when MySQL setup is unavailable. MySQL remains the default when this profile is not explicitly selected.

```powershell
mvn -f backend/pom.xml -Plocal-h2 spring-boot:run '-Dspring-boot.run.profiles=local-h2'
```

It creates a persistent H2 file in ignored `backend/data/`. It uses a separate H2 database; it neither connects to nor changes MySQL. Stop the backend with Ctrl+C. Do not use the local H2 profile as evidence that MySQL integration works.

## Start the frontend

In a second terminal from the repository root:

```powershell
python -m http.server 5500 --directory frontend
```

Open [http://localhost:5500](http://localhost:5500). The API default is `http://localhost:8080/api`. If the API host changes, configure `window.STUDENT_API_BASE_URL` in `frontend/index.html` before the module script and allow the frontend origin in `CORS_ALLOWED_ORIGINS`.

## Configuration reference

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | Local MySQL `student_management` JDBC URL | MySQL JDBC endpoint (not used by local H2 profile) |
| `DB_USERNAME` | `student_app` | MySQL account |
| `DB_PASSWORD` | Empty | MySQL password; set locally |
| `SERVER_PORT` | `8080` | Spring Boot HTTP port |
| `CORS_ALLOWED_ORIGINS` | localhost:5500 and 127.0.0.1:5500 | Allowed frontend origins |

## Stop services

Use Ctrl+C in each terminal. MySQL data persists in MySQL; local H2 data persists in ignored `backend/data/`.
