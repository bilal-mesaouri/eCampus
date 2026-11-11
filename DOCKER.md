# Docker

Add the PostgreSQL variables shown in `.env.example` to your ignored `.env` file,
preserving its existing values, and set a strong `JWT_SECRET`.
Build and start the application and PostgreSQL containers with Docker Compose:

```sh
docker compose up --build
```

The PostgreSQL data is stored in the `postgres-data` Docker volume and survives container
restarts. Stop the containers with `docker compose down`. To delete the database data too,
run `docker compose down -v`.

Running the app locally without a profile uses the `local` profile and an in-memory H2
database. Its data is reset when the app exits. To run outside Docker against PostgreSQL,
activate the `postgres` profile and set the `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` environment variables.

Jenkins and GitHub Actions explicitly activate `postgres` and run `./mvnw -B clean verify`
against PostgreSQL. The Jenkins agent needs Java 21, a Unix shell, Docker CLI access, and
permission to use a Docker daemon. Jenkins builds an image tagged `ecampus:<build-number>`;
publishing to a registry is not configured.