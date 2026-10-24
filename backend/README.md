# Backend

Spring Boot REST API for the Cognevance Student Management System. Student routes require HTTP Basic administrator authentication. The status-only `GET /actuator/health` route is unauthenticated for hosting probes. See the root [README](../README.md), [setup guide](../docs/SETUP.md), and [deployment readiness notes](../docs/DEPLOYMENT.md).

Run from the repository root: `mvn -f backend/pom.xml spring-boot:run`.
Run integration tests: `mvn -f backend/pom.xml test` (H2 test database).
