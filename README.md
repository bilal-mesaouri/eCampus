# eCampus monorepo

Two independent Spring Boot applications share a Maven parent and wrapper:

```text
pom.xml
mvnw
services/
  ecampus-service/
    pom.xml
    Dockerfile
    src/
  notification-service/
    pom.xml
    Dockerfile
    src/
```

- **eCampus:** the existing application, unchanged business behavior, port 8080.
- **Notifications:** a minimal standalone application on port 8081. Delivery,
  messaging, persistence, and HTTP endpoints are not implemented yet.

## Run separately

Use Java 21 and run these commands from the repository root in separate terminals:

```sh
./mvnw -pl services/ecampus-service spring-boot:run
```

```sh
./mvnw -pl services/notification-service spring-boot:run
```

eCampus requires a strong `JWT_SECRET` environment variable or an ignored
`services/ecampus-service/src/main/resources/application-local.properties` with
local configuration. Maven does not automatically load the root `.env` file.
Without an explicit profile, eCampus uses the local profile and in-memory H2.
The notification scaffold requires neither JWT configuration nor a database.

The root Maven reactor includes both services. Each service also has its own POM
and executable JAR, and neither depends on the other application.
See [DOCKER.md](DOCKER.md) for container startup and CI details.

## Git

Both services belong to the existing repository; do not initialize nested Git
repositories. Source files, module POMs, Dockerfiles, and shared configuration
can be committed together. Service `target/` directories, `.env`, and local
application configuration remain ignored.

After reviewing the changes:

```sh
git add -A
git diff --cached --stat
git commit -m "Restructure as a multi-service Maven monorepo"
git push
```

Before committing, review the staged diff and ensure it contains no credentials.
