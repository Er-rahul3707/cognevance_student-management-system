# Local setup

This guide runs the existing Spring Boot API and static frontend on Windows. MySQL 8 remains the default runtime database. The optional H2 profile is isolated and must be selected explicitly.

## Prerequisites

- JDK 17+ and Maven 3.6+.
- MySQL 8.x and an authorized local administrator for MySQL setup (unless using the optional H2 profile).
- Python 3 or another static HTTP server.
- Node.js 18+ for frontend checks.

## Administrator sign-in

The backend protects every route with HTTP Basic authentication. Set `APP_AUTH_USERNAME` to a nonblank local administrator name and `APP_AUTH_PASSWORD` to a unique random password of at least 16 characters in the same PowerShell terminal used to start Maven. The app BCrypt-encodes it in memory and does not persist a user record. The frontend prompts for these credentials; it retains the authorization header only in the current page memory and forgets it on refresh. A sign-in does not protect an unencrypted network connection: Basic authentication must only be used over HTTPS outside localhost. Do not put actual credentials in scripts, `.env.example`, tracked files, or chat.

```powershell
$env:APP_AUTH_USERNAME = 'admin'
$securePassword = Read-Host 'Enter a unique local password (16+ characters)' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try { $env:APP_AUTH_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer) }
```

The app intentionally fails startup if the username is blank or contains `:`, if the password is shorter than 16 characters, or if it exceeds BCrypt's 72-byte UTF-8 limit.

## MySQL database and application account

From the repository root, have an authorized MySQL administrator execute `database/schema.sql` in Workbench or the CLI. Replace the SQL file's clearly marked password placeholder with a strong local password before execution. The script creates the `student_management` schema and table and grants the dedicated `student_app` account only SELECT, INSERT, UPDATE, and DELETE. The default Spring profile validates the schema at startup; it does not create or migrate tables.

PowerShell command using the standard MySQL 8 installation path:

```powershell
cmd.exe /c '"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < database\schema.sql'
```

Alternatively, execute the file in MySQL Workbench. If `student_app` already exists, `CREATE USER IF NOT EXISTS` does not change its password.

For the reported MySQL 1045 case, connect in Workbench or the MySQL CLI as an authorized local MySQL administrator, then execute `database/schema.sql` after replacing its password placeholder locally. In Workbench, do not save the modified SQL file; for CLI use, make the temporary copy outside the repository and remove it afterward. After the schema script, execute the following as the same administrator, replacing the placeholder with that same new password. Never send the password in chat or commit it:

```sql
ALTER USER 'student_app'@'localhost' IDENTIFIED BY '<new-strong-local-password>';
GRANT SELECT, INSERT, UPDATE, DELETE ON student_management.* TO 'student_app'@'localhost';
```

The `ALTER USER` step resets only this project's dedicated account. If it reports that the account does not exist, rerun the schema with its placeholder replaced, then reapply the grant. If 1045 continues, have the administrator inspect the matching account hosts with `SELECT user, host, plugin FROM mysql.user WHERE user = 'student_app';` and ensure the account host matches the JDBC connection. Do not change another project's accounts or reuse its credentials.

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

For a hosted database, override `DB_URL` with the provider's hostname and TLS settings. Prefer Connector/J `sslMode=VERIFY_IDENTITY` with the provider's trusted certificate chain; do not use the local-development URL's `useSSL=false` or `allowPublicKeyRetrieval=true` settings for a remote database. Connector/J documents `VERIFY_IDENTITY` as encrypted transport plus certificate and hostname verification ([MySQL Connector/J security properties](https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-security.html)).

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
| `SERVER_PORT` | `8080` | Spring Boot HTTP port; `PORT` is accepted as a hosting-platform fallback |
| `CORS_ALLOWED_ORIGINS` | localhost:5500 and 127.0.0.1:5500 | Allowed frontend origins |
| `APP_AUTH_USERNAME` | Required | Single administrator username |
| `APP_AUTH_PASSWORD` | Required; 16–72 UTF-8 bytes | Administrator password, encoded with BCrypt in memory |

## Stop services

Use Ctrl+C in each terminal. MySQL data persists in MySQL; local H2 data persists in ignored `backend/data/`.
