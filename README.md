# Karma Platform

Karma is a wellness and spirituality events platform with:

- `frontend/`: React + TypeScript + Vite UI
- `backend/`: Spring Boot API
- `.agent/specs/karma-platform/`: product specs, design, and implementation plan

## Local development

1. Start the backend:

```bash
cd backend
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
- The implementation plan is tracked in [`.agent/specs/karma-platform/tasks.md`](/Users/zion/dev/project/karma/.agent/specs/karma-platform/tasks.md).
