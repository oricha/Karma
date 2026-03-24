# Karma Backend

Spring Boot backend for the Karma platform.

## Stack

- Java 21
- Spring Boot 3
- Spring Security with JWT
- Gradle
- Flyway
- In-memory seeded data store for local development
- PostgreSQL for local, test, and production runtime
- H2 only for automated tests

## Commands

```bash
./gradlew test
./gradlew bootRun
```

The API starts on `http://localhost:8081`.

## Profiles

- `local`: connects to PostgreSQL installed on the machine
- `test`: containerized PostgreSQL for the Dokploy environment
- `production`: containerized PostgreSQL for AWS deployment

You can override credentials with environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/karma_local
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

## Database bootstrap

Flyway now runs automatically on startup and creates seed tables plus sample events for July and August 2026 under:

- `src/main/resources/db/migration/V1__core_seed_schema.sql`
- `src/main/resources/db/migration/V2__seed_july_august_events.sql`

## Container deployment assets

- `Dockerfile`: backend image build
- `../deploy/test/docker-compose.yml`: Dokploy test stack
- `../deploy/production/docker-compose.yml`: AWS production stack

## Production workflow secrets

The AWS workflow expects these GitHub secrets:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION`
- `AWS_EC2_HOST`
- `AWS_EC2_USER`
- `AWS_EC2_SSH_KEY`
- `AWS_ECR_REPOSITORY`
- `PRODUCTION_DB_NAME`
- `PRODUCTION_DB_USER`
- `PRODUCTION_DB_PASSWORD`
- `PRODUCTION_APP_PORT`
- `PRODUCTION_FRONTEND_ORIGIN`
- `PRODUCTION_JWT_SECRET`

The Dokploy workflow expects:

```bash
DOKPLOY_DEPLOY_HOOK_URL=https://your-dokploy-instance.example.com/api/trpc/deployment.deploy?...
```

## Implemented API surface

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET/PUT /api/users/me`
- `GET/PUT /api/users/me/preferences`
- `GET /api/users/me/saved-events`
- `GET /api/users/me/orders`
- `GET /api/users/me/groups`
- `GET /api/users/me/events`
- `GET /api/categories`
- `GET /api/events`, `/api/events/popular`, `/api/events/nearby`, `/api/events/{slug}`
- `POST/DELETE /api/events/{id}/rsvp`
- `GET /api/groups`, `/api/groups/{slug}`
- `POST /api/groups/{id}/join`
- `GET /api/blog`, `/api/blog/featured`
- `POST /api/orders/checkout`
- `GET /api/organizers/me/dashboard`

## Demo credentials

- Email: `demo@karma.app`
- Password: `demo123`
