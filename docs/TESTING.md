# Verification record

Verification for this working revision was performed on 2026-10-25. Commands were run from the repository root unless noted. Automated checks, live H2 API checks, browser checks, and MySQL checks are reported separately.

## Automated tests

| Check | Result |
| --- | --- |
| `mvn -f backend/pom.xml test` | **Passed** — 15 tests, 0 failures/errors/skips; Spring MockMvc tests use in-memory H2 |
| `mvn -f backend/pom.xml package` | **Passed** — packaged successfully with Spring Boot 3.5.16; includes 15 backend tests |
| Bundled Node syntax checks for `frontend/js/app.js` and `frontend/js/student-utils.mjs` | **Passed** |
| Bundled Node `--test frontend/js/student-utils.test.mjs` | **Passed** — 8 tests, 0 failures; includes course filtering, sort modes, source-array immutability, and stale-session invalidation |
| `git diff --check` | **Passed** after final edits |
| npm scripts | Not runnable because npm is not on PATH; equivalent Node commands were used directly |

Backend automated tests cover REST CRUD, a committed create/read/delete persistence flow across separate HTTP requests, search, validation, malformed JSON, duplicate email handling, and error statuses. Frontend utility tests include the session-epoch primitive used to discard pending responses after sign-out, but remain pure Node tests and do not exercise the DOM. Backend tests use H2 and do not verify MySQL compatibility.

The security integration suite additionally checks the unauthenticated status-only `GET /actuator/health` route and rejects empty or wildcard CORS origin configuration. The CI workflow is configured at `.github/workflows/ci.yml`; no GitHub-hosted workflow result is claimed until an Actions run is observed.

Security integration tests verify that unauthenticated and incorrectly authenticated API reads and writes return 401, and that valid configured administrator credentials can read and submit a request (the latter reaches normal validation). They also check the configured CORS authorization preflight, reject invalid account configuration, and verify the public health route reveals an overall status but no component details. Test contexts use test-only credentials and H2. They do not validate production account provisioning or TLS termination.

## Live API and database verification

The explicit `local-h2` Maven profile started the real Spring Boot server with a persistent file-backed H2 database at `backend/data/` (ignored). The default application configuration remains MySQL with environment variables and `ddl-auto=validate`.

The earlier live HTTP workflow verified: initial empty list; POST create (201); list and get (200); search by name, email, course, and numeric ID; PUT update and lowercase email normalization; duplicate email conflict (409); invalid fields and malformed JSON (400); missing record (404); nonnumeric ID (400); DELETE (204); and empty list after deletion. A synthetic record survived a backend restart, confirming local H2 file persistence, and was then deleted. After authentication was added, a second live workflow verified anonymous 401, authenticated create/list/get/search/update/delete, invalid input 400, and missing-after-delete 404. For this release, the local file-backed H2 application was started on an isolated port with a generated, ephemeral administrator password; live HTTP checks confirmed health `UP`, anonymous API 401, authenticated create/read/search/update, duplicate email 409, invalid payload 400, delete 204, and post-delete 404. The synthetic record was deleted and the application stopped. All new synthetic records were deleted.

**MySQL is not verified.** Windows service `MYSQL80` is installed/running and port 3306 accepts TCP, but the isolated project account login previously failed with error 1045 and no MySQL administrator credentials were available. No other project's credentials were read or reused, and no MySQL schema/data were modified. To complete MySQL verification, an authorized local administrator must execute `database/schema.sql` (after setting its local placeholder password) or repair this project's dedicated account. Then set the project `DB_*` environment variables and run the default MySQL profile. H2 results must not be represented as MySQL integration.

## Manual browser verification

The frontend was served at `http://localhost:5500` and the Spring Boot backend ran on `http://127.0.0.1:8080` with the explicitly selected, isolated local H2 profile and temporary test-only administrator credentials. Browser automation completed login; checked the initial empty state; attempted an invalid email (native required/type validation prevented submission); created a synthetic student; verified the rendered list and dashboard totals; searched by email; filtered by course; opened details; edited the record and saw the updated values; opened the delete confirmation; confirmed deletion; and verified the empty state returned. Logout returned to the sign-in dialog, and a deliberately incorrect login showed a generic rejection message. Synthetic browser test data was deleted.

At a 390 × 844 viewport, the page initially had a 756px document scroll width because `.sr-only` lacked an explicit absolute position origin inside the table. Adding `left: 0; top: 0` removed document-level overflow: `document.documentElement.scrollWidth` measured 390px after the fix. The table itself remains horizontally scrollable inside `.table-wrap`, as intended. Browser console inspection reported zero warnings/errors during the tested workflow. This was local browser QA against H2, not MySQL.

## Commands

Run from the repository root:

```powershell
mvn -f backend/pom.xml verify
node --check frontend/js/app.js
node --check frontend/js/student-utils.mjs
node --test frontend/js/student-utils.test.mjs
git diff --check
```

For the local runtime/browser check, use the backend and frontend commands in [SETUP.md](SETUP.md). npm is not required to run the frontend utility tests directly.
