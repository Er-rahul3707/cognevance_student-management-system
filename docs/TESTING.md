# Verification record

Verification for this working revision was performed on 2026-10-24. Commands were run from the repository root unless noted. Automated checks, live H2 API checks, browser checks, and MySQL checks are reported separately.

## Automated tests

| Check | Result |
| --- | --- |
| `mvn -f backend/pom.xml test` | **Passed** — 7 tests, 0 failures/errors/skips; Spring MockMvc tests use in-memory H2 |
| `mvn -f backend/pom.xml package` | **Passed** — packaged with Spring Boot 3.5.16 after the framework upgrade |
| Bundled Node syntax checks for `frontend/js/app.js` and `frontend/js/student-utils.mjs` | **Passed** |
| Bundled Node `--test frontend/js/student-utils.test.mjs` | **Passed** — 5 tests, 0 failures |
| `git diff --check` | **Passed** after final edits |
| npm scripts | Not runnable because npm is not on PATH; equivalent Node commands were used directly |

Backend automated tests cover REST CRUD, a committed create/read/delete persistence flow across separate HTTP requests, search, validation, malformed JSON, duplicate email handling, and error statuses. They use H2 and do not verify MySQL compatibility.

## Live API and database verification

The explicit `local-h2` Maven profile started the real Spring Boot server with a persistent file-backed H2 database at `backend/data/` (ignored). The default application configuration remains MySQL with environment variables and `ddl-auto=validate`.

The live HTTP workflow verified: initial empty list; POST create (201); list and get (200); search by name, email, course, and numeric ID; PUT update and lowercase email normalization; duplicate email conflict (409); invalid fields and malformed JSON (400); missing record (404); nonnumeric ID (400); DELETE (204); and empty list after deletion. A synthetic record survived a backend restart, confirming local H2 file persistence, and was then deleted; list returned empty.

**MySQL is not verified.** Windows service `MYSQL80` is installed/running and port 3306 accepts TCP, but the isolated project account login previously failed with error 1045 and no MySQL administrator credentials were available. No other project's credentials were read or reused, and no MySQL schema/data were modified. To complete MySQL verification, an authorized local administrator must execute `database/schema.sql` (after setting its local placeholder password) or repair this project's dedicated account. Then set the project `DB_*` environment variables and run the default MySQL profile. H2 results must not be represented as MySQL integration.

## Manual browser verification

The frontend was served at `http://localhost:5500` and used the running Spring API with the explicit local-H2 profile. Browser automation observed the empty dashboard after cleanup and successfully exercised create, search by course, student details, update, and deletion after accepting the app's confirmation dialog. The UI showed its success message and the record disappeared after deletion. The UI-test record was synthetic and deleted. This is a local H2 browser workflow, not a MySQL browser integration test. No screenshot was committed.

## Commands

Run from the repository root:

```powershell
mvn -f backend/pom.xml test
mvn -f backend/pom.xml package
node --check frontend/js/app.js
node --check frontend/js/student-utils.mjs
node --test frontend/js/student-utils.test.mjs
git diff --check
```

For the local runtime/browser check, use the backend and frontend commands in [SETUP.md](SETUP.md). npm is not required to run the frontend utility tests directly.
