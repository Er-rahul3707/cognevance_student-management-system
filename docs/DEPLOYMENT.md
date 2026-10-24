# Deployment readiness

**Status: not deployed.** No hosting resources were created, and there is no live application URL. This guide describes a safe deployment shape; it does not provision services or publish credentials.

## Current application constraints

- The frontend is static HTML/CSS/JavaScript and can be served from a static-site host.
- The backend is a Spring Boot 3.5.x Java 17 service. It listens on `PORT` when provided, otherwise `SERVER_PORT`/8080.
- MySQL is the default production database. The local H2 profile is file-backed and must not run on an ephemeral host filesystem.
- `GET /actuator/health` exposes only the aggregate health status for a host probe. Student endpoints remain protected by HTTP Basic.
- Administrator credentials are a single in-memory account sourced from environment variables. There is no persistent user store, password rotation UI, rate limiting, audit trail, or fine-grained role model. That is a material limitation for public production use.
- MySQL integration has not been verified in this environment because the existing local account failed with MySQL error 1045. Deployment must wait until a persistent MySQL service and valid dedicated account are provisioned and tested.

## Hosting option assessment

For a low-cost demonstration, the static frontend and Java API can be hosted as separate services, with a separate persistent MySQL database. Render currently offers free static sites and free web services, but its free web services spin down after 15 minutes idle and have an ephemeral filesystem; free services cannot attach persistent disks. Its free relational database offering is PostgreSQL and expires after 30 days, so it does not meet this application's MySQL requirement. Render supports Docker-based web services, but this repository has no Dockerfile and is not directly configured for that platform. See [Render free services and their limits](https://render.com/docs/free), [web services](https://render.com/docs/web-services), and [persistent disks](https://render.com/docs/disks).

Therefore, a fully persistent, no-cost Render deployment of this unchanged MySQL application is not established. A demo is possible only if an independently selected MySQL provider offers suitable persistent storage, network access, and TLS at no cost; verify that provider's current terms and connection requirements before use. A paid persistent MySQL service is another option, but no paid resources should be created without explicit authorization. Free hosting is not a production availability guarantee.

## Required deployment configuration

Build the service using the repository's tested Java version:

```powershell
mvn -f backend/pom.xml clean verify
```

Run the packaged artifact with these host-provided environment variables (values belong in the host's secret manager, not in source control):

| Variable | Requirement |
| --- | --- |
| `PORT` or `SERVER_PORT` | Platform-assigned listening port; the application already honors `PORT` |
| `APP_AUTH_USERNAME` | Nonblank administrator username without `:` |
| `APP_AUTH_PASSWORD` | At least 16 Unicode characters and no more than 72 UTF-8 bytes |
| `DB_URL` | Remote MySQL JDBC URL with the provider's host and TLS verification, e.g. Connector/J `sslMode=VERIFY_IDENTITY` |
| `DB_USERNAME` / `DB_PASSWORD` | Dedicated least-privilege account; never use a database administrator account |
| `CORS_ALLOWED_ORIGINS` | Exact HTTPS origin serving the frontend; no wildcard |

For example, set the remote JDBC URL in the hosting dashboard in this form, substituting the actual provider endpoint:

```text
jdbc:mysql://<mysql-host>:3306/student_management?serverTimezone=UTC&sslMode=VERIFY_IDENTITY
```

Run `java -jar backend/target/student-management-1.0.0.jar` as the service start command after the build. Do not set `spring.jpa.hibernate.ddl-auto=update` for production; the default MySQL configuration uses `validate`, and schema changes remain operator-managed through a reviewed migration/setup procedure. Do not use `useSSL=false`, `allowPublicKeyRetrieval=true`, an embedded H2 file, or an ephemeral local filesystem for hosted student data.

Serve the `frontend/` directory as a static site over HTTPS. Before deployment, configure `window.STUDENT_API_BASE_URL` to the API's HTTPS `/api` base URL and set `CORS_ALLOWED_ORIGINS` to that exact frontend origin. Ensure the database accepts traffic only from the backend service or trusted network and supports verified TLS.

## Readiness checks before any public release

1. Resolve the project's MySQL 1045 issue using an authorized database administrator and the documented `database/schema.sql` procedure.
2. Verify MySQL connection, schema validation, CRUD, and persistence after a backend restart using the dedicated app account.
3. Replace single-admin Basic authentication with a managed identity solution or otherwise explicitly accept and mitigate the single-account limitations; require TLS end to end.
4. Configure provider secrets, exact CORS origin, outbound database network rules, backups, and a restoration plan.
5. Deploy only after explicit owner authorization, then verify HTTPS, health probe, authenticated CRUD, and persistent data on the actual service.

## CI

The repository includes [GitHub Actions CI](../.github/workflows/ci.yml), which runs Maven verification plus JavaScript syntax and utility tests on pushes to `main`, pull requests, and manual dispatches. This workflow definition does not itself prove that a GitHub-hosted run completed successfully; check the Actions page for a run result after publication.
