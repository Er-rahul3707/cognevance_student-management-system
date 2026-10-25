# Deploy to Render

**Deployment status: not deployed.** The earlier Hibernate error is a database configuration failure, not a missing Hibernate dialect. This application is MySQL-only: it reads `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. If `DB_URL` is missing it falls back to `localhost`, which is the Render app container itself; Render cannot reach a MySQL server on your PC. A Render Postgres `DATABASE_URL` is also not a MySQL JDBC URL and cannot be used by this build.

The repository now has a Render Blueprint at [`render.yaml`](../render.yaml). It builds one Docker web service that serves both the Spring Boot API and the frontend from the same HTTPS origin. This avoids a separate frontend/API CORS configuration. The Blueprint deliberately prompts for secrets and a MySQL URL; it does not create the database or deploy anything until you create the Blueprint in Render.

## Required Render resources

The application needs a reachable MySQL 8 database. Create a MySQL private service in the same Render workspace and region as the web service. Render's MySQL guide uses a Docker-based private service and a persistent disk mounted at `/var/lib/mysql`; persistent database storage can incur a charge. Review the current [Render MySQL guide](https://render.com/docs/deploy-mysql), [disk documentation](https://render.com/docs/disks), and pricing before creating resources.

On first initialization, configure the MySQL service with:

| MySQL service variable | Value |
| --- | --- |
| `MYSQL_DATABASE` | `student_management` |
| `MYSQL_USER` | `student_app` |
| `MYSQL_PASSWORD` | A newly generated database password |
| `MYSQL_ROOT_PASSWORD` | A separate newly generated administrator password |

Keep the MySQL service private; do not expose its port publicly. Add a persistent disk mounted at `/var/lib/mysql`. Render documents the internal connection hostname on the MySQL service page; it has a form such as `mysql-example:3306`.

Create the application table once after MySQL starts. In the MySQL service Shell, connect locally as its administrator (`mysql -u root -p`), enter the root password at the prompt, and run:

```sql
USE student_management;
CREATE TABLE IF NOT EXISTS students (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL,
  phone VARCHAR(30),
  course VARCHAR(100) NOT NULL,
  enrollment_date DATE NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uk_students_email UNIQUE (email)
);
```

The MySQL image creates the configured `MYSQL_USER` for the configured database on first initialization. Do not run the local-only `database/schema.sql` unchanged on Render: its account host is `localhost`, which is not the application container. The application expects the account created for the MySQL service.

## Create the web service

1. In Render, choose **New → Blueprint** and select this repository's `main` branch. Render reads `render.yaml` and builds the root `Dockerfile`.
2. In the Blueprint environment form, set `DB_URL` to the MySQL service's **internal** hostname, for example `jdbc:mysql://mysql-example:3306/student_management?serverTimezone=UTC`. Replace the host with the actual private hostname shown by Render. Do not use `localhost`, a PostgreSQL URL, or a URL copied from another project.
3. Supply `DB_PASSWORD`, `APP_AUTH_USERNAME`, and `APP_AUTH_PASSWORD` in Render's secret prompts. Use the same MySQL password configured as `MYSQL_PASSWORD`; create a separate administrator password of at least 16 characters and no more than 72 UTF-8 bytes. `DB_USERNAME` is set to `student_app` in the Blueprint.
4. Choose a web-service plan and review the price before creating it. The Blueprint sets the health check path to `/actuator/health` and the Render port to `10000`.
5. Create the service only after the MySQL instance is reachable and the schema exists. The app keeps Hibernate `ddl-auto=validate`, so it will fail startup rather than silently alter a production schema.

Render stores `sync: false` values in the service configuration and prompts for them when a Blueprint is first created. Do not put secret values in `render.yaml`, Git, issues, or chat. See [Render environment variables and secrets](https://render.com/docs/configure-environment-variables) and the [Blueprint specification](https://render.com/docs/blueprint-spec).

The single service serves the dashboard at `/` and the protected API at `/api`. The frontend selects the same-origin API URL when hosted this way. In local development, `http://localhost:5500` continues to call `http://localhost:8080/api`. The backend listens on Render's `PORT` and binds to `0.0.0.0`.

## After deployment

Verify the service's actual Render URL, `GET /actuator/health`, the sign-in screen, and authenticated student CRUD. The health endpoint reports application health; it is not proof that MySQL data persists across redeploys. Verify persistence by creating a synthetic record, redeploying/restarting the web service, and retrieving the record afterward. Do not use real student data for smoke tests.

The local screenshot error should disappear once Render has a valid `DB_URL`, matching credentials, a reachable MySQL service, and the `students` table. Do **not** try to fix missing JDBC metadata by setting `hibernate.dialect` manually; that can hide the missing connection configuration but cannot make the database reachable. If startup still fails, inspect the first `Communications link failure`, `Access denied`, DNS, or missing-table message below the Hibernate wrapper and correct that specific cause.

## Remaining production considerations

- This app uses one in-memory administrator account and HTTP Basic authentication. Render terminates HTTPS for the public web URL, but this is still a demo-grade access model: there is no user lifecycle, rate limiting, audit log, or per-user authorization. Do not use it for sensitive student records without a stronger identity and authorization design.
- Configure backups and test restoration for the MySQL service. A persistent disk alone is not a backup strategy.
- The Docker image is built in GitHub Actions. Successful CI verifies the build, not the Render environment, database connectivity, or a live deployment.
- A free web-service plan may spin down; a persistent MySQL service with a disk may be billed. Confirm current plan limits and pricing before creating resources.

## CI status

GitHub Actions runs Maven verification, frontend checks, and a Docker build on pushes to `main`. Check the repository's [Actions page](https://github.com/Er-rahul3707/cognevance_student-management-system/actions) for the result corresponding to the commit you deploy.
