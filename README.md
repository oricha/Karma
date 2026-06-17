# Karma Platform

Karma is a wellness and spirituality events platform with:

- `frontend/`: React + TypeScript + Vite UI
- `backend/`: Spring Boot API
- `.agent/specs/karma-platform/`: product specs, design, and implementation plan

## Local development

### Database

Local development uses a native PostgreSQL 16 + PostGIS install on `localhost:5432`.

```bash
brew install postgresql@16 postgis
brew services start postgresql@16
psql postgres -c "CREATE ROLE postgres WITH LOGIN SUPERUSER PASSWORD 'postgres';" 2>/dev/null || true
psql postgres -c "CREATE DATABASE karma_local OWNER postgres;"
psql -d karma_local -c "CREATE EXTENSION IF NOT EXISTS postgis;"
```

The root `docker-compose.yml` is kept only as an optional helper for developers who still want Docker PostgreSQL/MailHog locally. Email is disabled by default in the `local` profile; set `KARMA_EMAIL_ENABLED=true` and run MailHog if you need to inspect local email.

### App

1. Start the backend:

```bash
cd backend
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/karma_local
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export KARMA_JWT_SECRET=replace-with-a-long-random-secret
export KARMA_EMAIL_ENABLED=false
./gradlew bootRun
```

2. Start the frontend:

```bash
cd frontend
npm install
npm run dev
```

The frontend proxies `/api` requests to `http://localhost:8081`.

## Demo credentials

- Email: `demo@karma.app`
- Password: `demo123`

## Notes

- The backend now runs on the persistent JPA/PostgreSQL domain model; `PlatformDataStore` is no longer the runtime source.
- Flyway provisions the local database schema plus sample July/August 2026 events.
- Local development now targets a PostgreSQL instance installed on the machine through the `local` Spring profile.
- PostgreSQL runtime is now standardized on PostgreSQL 16 + PostGIS for `local`, `test`, and `production`.
- Backend phase 1 also introduces locale resolution, S3-compatible storage abstractions, and a provider-based geocoding layer.
- Backend phase 6 introduces OpenAPI at `/swagger-ui.html`, health checks at `/actuator/health`, JaCoCo coverage reports, stricter security headers, JWT audience validation, and a repo-level pre-commit secret scan hook under `.githooks/pre-commit`.
- `deploy/test/docker-compose.yml` provisions the Dokploy test stack with backend + frontend containers that connect to Neon test.
- Production deploys are handled by Railway for the backend and Vercel for the frontend, both connected to Neon prod.
- GitHub Actions: `ci.yml` runs backend tests + JaCoCo plus frontend lint/test/build; `deploy-test-dokploy.yml` builds and pushes backend/frontend images for the test environment.
- Phase 5 adds trilingual email (ES/EN/CA), weekly digests, event reminders, blog, and group discussions. Schedulers default to **off** until enabled via env vars.
- The implementation plan is tracked in [`.agent/specs/karma-platform/tasks.md`](/Users/zion/dev/project/karma/.agent/specs/karma-platform/tasks.md).
