# Karma Platform

Karma is a wellness and spirituality events platform with:

- `frontend/`: React + TypeScript + Vite UI
- `backend/`: Spring Boot API
- `.agent/specs/karma-platform/`: product specs, design, and implementation plan

## Local development

1. Start the backend:

```bash
cd backend
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/karma_local
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export KARMA_JWT_SECRET=replace-with-a-long-random-secret
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
- `deploy/test/docker-compose.yml` provisions the test stack for Dokploy.
- `deploy/production/docker-compose.yml` provisions the production stack for AWS EC2.
- GitHub Actions workflows live in `.github/workflows/` for test Dokploy deployment and production AWS deployment.
- The implementation plan is tracked in [`.agent/specs/karma-platform/tasks.md`](/Users/zion/dev/project/karma/.agent/specs/karma-platform/tasks.md).
