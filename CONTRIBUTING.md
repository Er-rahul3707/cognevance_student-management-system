# Contributing

Thanks for helping improve this internship project. Keep changes focused, follow the existing controller/service/repository structure, and update the API, setup, or architecture documentation when behavior changes.

## Local workflow

1. Create a feature branch from `main`.
2. Configure a dedicated local MySQL schema using [docs/SETUP.md](docs/SETUP.md); do not point the app at another project's database.
3. Run `mvn -f backend/pom.xml test`, `mvn -f backend/pom.xml package`, `npm --prefix frontend run check`, and `npm --prefix frontend test`.
4. If MySQL is configured, manually verify the frontend against the real API and report that check separately from H2 tests.
5. Open a pull request with a concise summary and the exact verification commands and outcomes.

Do not commit credentials, `.env` files, real student records, personal data, generated build output, or screenshots that expose personal data. Do not report unrun checks as passing.
