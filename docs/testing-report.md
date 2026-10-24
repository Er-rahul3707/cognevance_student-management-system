# Testing report

## Automated checks

`mvn -f backend/pom.xml test` — **passed** on 2026-10-24. Three Spring Boot/MockMvc integration tests ran against an in-memory H2 database; 0 failures, 0 errors, 0 skipped. Coverage includes create, search, individual read, update, delete/not-found, required-field and malformed JSON validation, and case-insensitive duplicate-email handling.

`mvn -f backend/pom.xml package` — **passed** on 2026-10-24. The Spring Boot jar was created at `backend/target/student-management-1.0.0.jar` (ignored by Git).

`node --check frontend/js/app.js` — **passed** on 2026-10-24.

## Manual checks

After configuring MySQL and starting the API and static frontend, verify the initial empty state, create a student, search by each supported field, open a record, edit it, attempt a duplicate email, delete with confirmation, and resize the browser to mobile width. Confirm the API origin matches the configured CORS origin.

## Environment verification status

MySQL was not available in this environment, so the API was not started against MySQL and no browser CRUD session or genuine app screenshot was captured. The H2 integration tests verify application behavior but do not replace a MySQL compatibility check. GitHub CLI is also unavailable and no remote is configured, so no GitHub repository was created or push performed.

