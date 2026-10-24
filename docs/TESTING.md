# Verification record

Run the checks from the repository root. Results below were recorded on 2026-10-24 for this revision.

## Automated checks

| Check | Result |
| --- | --- |
| `mvn -f backend/pom.xml test` | **Passed** — 6 tests, 0 failures, 0 errors, 0 skipped; Spring MockMvc and in-memory H2 |
| `mvn -f backend/pom.xml package` | **Passed** — build completed; executable jar created under ignored `backend/target/` |
| Bundled Node `--check frontend/js/app.js` | **Passed** |
| Bundled Node `--check frontend/js/student-utils.mjs` | **Passed** |
| Bundled Node `--test frontend/js/student-utils.test.mjs` | **Passed** — 5 tests, 0 failures |
| `git diff --check` | **Passed** after removing trailing whitespace/blank lines |
| `npm --prefix frontend run check` / `npm --prefix frontend test` | Not runnable here: `npm` is not exposed in the shell. The matching Node syntax and test files were run directly and passed. |

The Node runtime used was the bundled Codex Node executable. In a normal Node.js installation, `npm --prefix frontend run check` and `npm --prefix frontend test` execute those same checks.

The backend integration suite uses H2 and Spring MockMvc. It covers CRUD, search by name/email/course/numeric ID, case-insensitive email normalization and duplicates, invalid fields, malformed JSON, and malformed path IDs. H2 tests do not prove MySQL connectivity or MySQL-specific compatibility.

## Manual and environment checks

A manual browser CRUD session and real MySQL integration check have not been performed for this revision. The host has a running MySQL service, but the available default database environment points to a separate CollegeWise database. This project must not use or alter that schema; configure a dedicated `student_management` schema and explicit credentials before validating its MySQL integration. No runtime screenshot or deployment is claimed.

## Reproducible checks

```powershell
mvn -f backend/pom.xml test
mvn -f backend/pom.xml package
npm --prefix frontend run check
npm --prefix frontend test
git diff --check
```

The H2 profile is test-only; the application runtime remains MySQL.
