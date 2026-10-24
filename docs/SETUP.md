# Local setup

These steps run the existing application locally with MySQL 8, Spring Boot, and a static frontend server.

## 1. Requirements

- JDK 17+ and Maven 3.6+.
- MySQL 8.x server and an administrator account for initial setup.
- Python 3 or another static HTTP server.
- Node.js 18+ for frontend utility tests.

## 2. Create the database

From the repository root, run `database/schema.sql` in MySQL Workbench or the MySQL CLI as an administrator. Before running it, replace the `replace_with_a_local_password` placeholder in the `CREATE USER` statement with a strong local password. The script creates `student_management`, the `students` table, and a restricted `student_app` account. The application account is granted SELECT, INSERT, UPDATE, and DELETE only. The database administrator creates the schema; the application validates it and does not perform schema migrations.

For the CLI from PowerShell, run this through `cmd.exe` so Windows shell input redirection is handled by the command interpreter (run from the repository root):

```powershell
cmd.exe /c '"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < database\schema.sql'
```

Alternatively, open `database/schema.sql` in MySQL Workbench while connected as an administrator and execute it there.

## 3. Configure backend environment

Copy `.env.example` as a reference if useful, but Spring Boot does not automatically load `.env`. Set variables in the same PowerShell terminal where you run Maven (replace the password with the value used in SQL):

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/student_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:DB_USERNAME = 'student_app'
$env:DB_PASSWORD = 'your-local-password'
$env:SERVER_PORT = '8080'
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5500,http://127.0.0.1:5500'
mvn -f backend/pom.xml spring-boot:run
```

For a host other than localhost, adjust the MySQL account host grant and `DB_URL` deliberately. Do not reuse unrelated database credentials or point this application at another project's schema.

## 4. Start the frontend

In a second terminal from the repository root:

```powershell
python -m http.server 5500 --directory frontend
```

Browse to [http://localhost:5500](http://localhost:5500). The API default is `http://localhost:8080/api`. For a different API base URL, configure `window.STUDENT_API_BASE_URL` in `frontend/index.html` before the module script. Ensure `CORS_ALLOWED_ORIGINS` matches the browser origin.

## 5. Stop the services

Stop each terminal process with Ctrl+C. The data remains in MySQL between runs.

## Configuration reference

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | Local `student_management` JDBC URL | MySQL JDBC endpoint |
| `DB_USERNAME` | `student_app` | Database account |
| `DB_PASSWORD` | Empty | Database password; set locally |
| `SERVER_PORT` | `8080` | Spring Boot HTTP port |
| `CORS_ALLOWED_ORIGINS` | localhost:5500 and 127.0.0.1:5500 | Comma-separated allowed frontend origins |
