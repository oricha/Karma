# Karma Backend

Spring Boot backend for the Karma platform.

## Commands

```bash
./gradlew test
./gradlew jacocoTestReport
./gradlew bootRun
```

The API starts on `http://localhost:8081`.

## Profiles

- `local`: connects to PostgreSQL via `docker compose` on `localhost:5433` (or override for a native install on 5432)
- `test`: containerized PostgreSQL for the Dokploy environment
- `production`: containerized PostgreSQL for AWS deployment

All runtime profiles now target PostgreSQL 16 with PostGIS enabled. Automated backend tests still use H2 and skip Flyway so PostGIS-specific migrations do not break CI.

You can override credentials with environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/karma_local
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
KARMA_JWT_SECRET=replace-with-a-long-random-secret
```

## Security and observability

- OpenAPI JSON: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`
- Health endpoint: `GET /actuator/health`
- JaCoCo HTML coverage report: `build/reports/jacoco/test/html/index.html`

Phase 6 hardening now adds:

- strict security headers (`CSP`, `HSTS`, `X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy`)
- JWT audience validation in addition to issuer/signature/expiry checks
- restricted CORS based on `KARMA_FRONTEND_ORIGIN`
- HTTPS enforcement toggle via `KARMA_SECURITY_REQUIRE_HTTPS`
- removal of unsafe production/test defaults for datasource credentials and JWT secret

To enable the repository pre-commit secret scan hook:

```bash
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit
```

## Phase 1 infrastructure variables

Internationalization:

- `KARMA_DEFAULT_LOCALE`
- `KARMA_LOCALE_COOKIE_NAME`

S3-compatible storage:

- `KARMA_STORAGE_ENABLED`
- `KARMA_STORAGE_BUCKET`
- `KARMA_STORAGE_REGION`
- `KARMA_STORAGE_ENDPOINT`
- `KARMA_STORAGE_ACCESS_KEY`
- `KARMA_STORAGE_SECRET_KEY`
- `KARMA_STORAGE_PUBLIC_BASE_URL`
- `KARMA_STORAGE_MAX_UPLOAD_SIZE_BYTES`

Geocoding:

- `KARMA_GEOCODING_ENABLED`
- `KARMA_GEOCODING_PROVIDER`
- `KARMA_GEOCODING_BASE_URL`
- `KARMA_GEOCODING_USER_AGENT`
- `KARMA_GEOCODING_TIMEOUT_MILLIS`
- `KARMA_GEOCODING_MAX_RETRIES`
- `KARMA_GEOCODING_CACHE_SIZE`

## Database bootstrap

Flyway now runs automatically on startup and creates seed tables plus sample events for July and August 2026 under:

- `src/main/resources/db/migration/V1__core_seed_schema.sql`
- `src/main/resources/db/migration/V2__seed_july_august_events.sql`
- `src/main/resources/db/migration/V3__enable_postgis_and_geo_indexes.sql`

For local PostgreSQL installations you must also have the `postgis` extension available:

```sql
CREATE DATABASE karma;
\c karma
CREATE EXTENSION IF NOT EXISTS postgis;
```

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
- `KARMA_SECURITY_REQUIRE_HTTPS`

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
