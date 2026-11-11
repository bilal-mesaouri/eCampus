# Docker

Add the PostgreSQL variables shown in `.env.example` to your ignored `.env` file,
preserving its existing values, and set a strong `JWT_SECRET`.
Build and start both applications and PostgreSQL with Docker Compose:

```sh
docker compose up --build
```

eCampus is available on port 8080 and the notification service on port 8081.
The notification service is a standalone scaffold: it has no notification endpoints,
database, or connection to eCampus yet. See [README.md](README.md) for running each
application separately without Docker.

Start only eCampus and its database:

```sh
docker compose up --build ecampus
```

Start only the notification service:

```sh
docker compose up --build notifications
```

Compose interpolates the entire file even when starting only notifications, so it
still requires the root `.env` values. The notification application itself does not
require PostgreSQL or `JWT_SECRET`.

Both Dockerfiles use the repository root as the build context:

```sh
docker build -f services/ecampus-service/Dockerfile -t ecampus:local .
docker build -f services/notification-service/Dockerfile -t ecampus-notifications:local .
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
permission to use a Docker daemon. Jenkins builds images tagged `ecampus:<build-number>`
and `ecampus-notifications:<build-number>`; publishing to a registry is not configured.