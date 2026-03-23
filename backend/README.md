# Karma Backend

Spring Boot backend for the Karma platform.

## Stack

- Java 21
- Spring Boot 3
- Spring Security with JWT
- Gradle
- Flyway
- In-memory seeded data store for local development
- H2 for local Flyway migrations, PostgreSQL-ready driver included

## Commands

```bash
./gradlew test
./gradlew bootRun
```

The API starts on `http://localhost:8081`.

## Database bootstrap

Flyway now runs automatically on startup and creates seed tables plus sample events for July and August 2026 under:

- `src/main/resources/db/migration/V1__core_seed_schema.sql`
- `src/main/resources/db/migration/V2__seed_july_august_events.sql`

By default the backend uses a local H2 file database. You can override it with PostgreSQL environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/karma
SPRING_DATASOURCE_USERNAME=karma
SPRING_DATASOURCE_PASSWORD=karma
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
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
