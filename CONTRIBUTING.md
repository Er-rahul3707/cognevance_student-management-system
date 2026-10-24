# Contributing

Thanks for helping improve this internship project. Keep changes focused, use the existing controller/service/repository layers, and update API or setup documentation when behavior changes.

## Local workflow

1. Create a feature branch.
2. Configure MySQL using `.env.example` and your local environment.
3. Run `mvn -f backend/pom.xml test` and `mvn -f backend/pom.xml package`.
4. Manually verify frontend behavior against the local API.
5. Open a pull request with a concise summary and verification notes.

Do not commit credentials, `.env` files, personal student data, or generated build output.

