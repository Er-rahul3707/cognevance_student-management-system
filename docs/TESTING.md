# Verification record

Runtime verification and automated checks for this revision were performed on 2026-10-24. Commands were run from the repository root unless noted.

## Automated tests

| Check | Result |
| --- | --- |
| `mvn -f backend/pom.xml test` | **Passed** — 6 Spring MockMvc integration tests, 0 failures, 0 errors, 0 skipped; tests use in-memory H2 |
| `mvn -f backend/pom.xml package` | **Passed** on the preceding verification run; executable jar is generated under ignored `backend/target/` |
| Bundled Node `--check frontend/js/app.js` | **Passed** |
| Bundled Node `--check frontend/js/student-utils.mjs` | **Passed** |
| Bundled Node `--test frontend/js/student-utils.test.mjs` | **Passed** — 5 tests, 0 failures |
| `git diff --check` | **Passed** |
| `npm --prefix frontend run check` / `npm --prefix frontend test` | Not runnable in this shell because `npm` is not on PATH. The equivalent Node syntax checks and test file were run directly with the bundled Node runtime. |

The backend tests exercise create, list, get, update, delete/not-found, search by name/email/course/numeric ID, case-insensitive email normalization and duplicate detection, invalid fields, malformed JSON, and invalid numeric IDs. These are automated tests against H2, not real MySQL.

## Database integration

MySQL 8 is installed; Windows service `MYSQL80` was running and TCP port 3306 accepted connections. The MySQL CLI is installed. No saved MySQL login path was configured, and a noninteractive root login without a password failed. A read-only login probe to the isolated `student_management` database as `student_app` without a password failed with MySQL `ERROR 1045 (28000): Access denied ... (using password: NO)`. No credentials from the separate CollegeWise application were used. No schema was created or modified, and no student data was read or written.

The backend was attempted with temporary process-only variables targeting `jdbc:mysql://127.0.0.1:3306/student_management` and `student_app`, with an empty password. Startup failed because the app account could not authenticate. Hibernate is configured to `validate`, so that attempt did not modify a database.

**MySQL CRUD and persistence are unverified.** To continue, use a local MySQL administrator account in MySQL Workbench or the CLI to execute `database/schema.sql` after setting its placeholder to a new local password. Then set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the backend terminal to that dedicated account and rerun the backend and API checks. If the `student_app` account already exists, note that `CREATE USER IF NOT EXISTS` will not update its password; reset only the dedicated project account or create a separate dedicated account with DML grants for `student_management`. Do not reuse CollegeWise credentials or schema.

## Manual browser verification

The frontend server started with `python -m http.server 5500 --directory frontend`, and the dashboard page rendered in the browser at `http://localhost:5500/`. Its initial API request displayed the expected generic “Could not load students / Failed to fetch” state because the backend could not start without database authentication. Therefore manual browser CRUD, validation, and API error handling were **not** exercised against a running backend. No screenshot or successful live end-to-end session is claimed.

## Commands

```powershell
mvn -f backend/pom.xml test
mvn -f backend/pom.xml package
npm --prefix frontend run check
npm --prefix frontend test
git diff --check
```

The application runtime remains MySQL. H2 is only used for automated tests; no runtime database substitution was made.
