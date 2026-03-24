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

- The backend currently uses seeded in-memory data so the frontend can be connected end-to-end while the platform is still being built out.
- Flyway now provisions a local database schema plus sample July/August 2026 events for the eventual persistence layer.
- Local development now targets a PostgreSQL instance installed on the machine through the `local` Spring profile.
- `deploy/test/docker-compose.yml` provisions the test stack for Dokploy.
- `deploy/production/docker-compose.yml` provisions the production stack for AWS EC2.
- GitHub Actions workflows live in `.github/workflows/` for test Dokploy deployment and production AWS deployment.
- The implementation plan is tracked in [`.agent/specs/karma-platform/tasks.md`](/Users/zion/dev/project/karma/.agent/specs/karma-platform/tasks.md).
