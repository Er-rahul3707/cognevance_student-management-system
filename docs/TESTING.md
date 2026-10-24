# Verification record

Verification for this working revision was performed on 2026-10-25. Commands were run from the repository root unless noted. Automated checks, live H2 API checks, browser checks, and MySQL checks are reported separately.

## Automated tests

| Check | Result |
| --- | --- |
| `mvn -f backend/pom.xml test` | **Passed** — 13 tests, 0 failures/errors/skips; Spring MockMvc tests use in-memory H2 |
| `mvn -f backend/pom.xml package` | **Passed** — package built successfully with Spring Boot 3.5.16 |
| Bundled Node syntax checks for `frontend/js/app.js` and `frontend/js/student-utils.mjs` | **Passed** after the current frontend changes |
| Bundled Node `--test frontend/js/student-utils.test.mjs` | **Passed** — 7 tests, 0 failures; includes course filtering, sort modes, and source-array immutability |
| `git diff --check` | **Passed** after final edits |
| npm scripts | Not runnable because npm is not on PATH; equivalent Node commands were used directly |

Backend automated tests cover REST CRUD, a committed create/read/delete persistence flow across separate HTTP requests, search, validation, malformed JSON, duplicate email handling, and error statuses. The current UI's sort/filter utility tests are pure Node tests and do not exercise the DOM. Backend tests use H2 and do not verify MySQL compatibility.

Security integration tests verify that unauthenticated and incorrectly authenticated API reads and writes return 401, and that valid configured administrator credentials can read and submit a request (the latter reaches normal validation). They also check the configured CORS authorization preflight and reject invalid account configuration. Test contexts use test-only credentials and H2. They do not validate production account provisioning or TLS termination.

## Live API and database verification

The explicit `local-h2` Maven profile started the real Spring Boot server with a persistent file-backed H2 database at `backend/data/` (ignored). The default application configuration remains MySQL with environment variables and `ddl-auto=validate`.

The earlier live HTTP workflow verified: initial empty list; POST create (201); list and get (200); search by name, email, course, and numeric ID; PUT update and lowercase email normalization; duplicate email conflict (409); invalid fields and malformed JSON (400); missing record (404); nonnumeric ID (400); DELETE (204); and empty list after deletion. A synthetic record survived a backend restart, confirming local H2 file persistence, and was then deleted. After authentication was added, a second live workflow verified anonymous 401, authenticated create/list/get/search/update/delete, invalid input 400, and missing-after-delete 404. All new synthetic records were deleted.

**MySQL is not verified.** Windows service `MYSQL80` is installed/running and port 3306 accepts TCP, but the isolated project account login previously failed with error 1045 and no MySQL administrator credentials were available. No other project's credentials were read or reused, and no MySQL schema/data were modified. To complete MySQL verification, an authorized local administrator must execute `database/schema.sql` (after setting its local placeholder password) or repair this project's dedicated account. Then set the project `DB_*` environment variables and run the default MySQL profile. H2 results must not be represented as MySQL integration.

## Manual browser verification

Before administrator authentication was added, the frontend was served at `http://localhost:5500` and browser automation exercised create, search by course, student details, update, and confirmed deletion against the local H2 API. This historical run does not verify the current authenticated UI. For the current revision, the static frontend server returned HTTP 200, and Chrome headless captured the real sign-in view at `screenshots/sign-in.png` (1440 × 1000); the image was opened and visually checked. Administrator credentials were not entered into browser automation, so sign-in success and authenticated browser CRUD were not tested in this run. The current frontend JavaScript and pure list utility functions are syntax/test checked; authenticated CRUD remains verified over HTTP only, not through a browser.

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
