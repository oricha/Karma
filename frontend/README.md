# Karma Frontend

React + TypeScript frontend for the Karma platform.

## Commands

```bash
npm install
npm run dev
npm run build
npm run test
```

## Environment

- `VITE_API_URL` (optional): overrides the backend base URL.
- By default, development uses the Vite proxy and forwards `/api` to `http://localhost:8081`.
- In Dokploy test, `VITE_API_URL` is passed at Docker build time with the public test backend URL.
- In Vercel production, set `VITE_API_URL` as a project environment variable pointing to the Railway backend URL.

## Deployment

- `Dockerfile` builds the frontend for the Dokploy test environment and serves it with nginx.
- `vercel.json` rewrites all routes to `index.html` so React Router works on Vercel.

## Current integration status

- Event, group, category, blog, auth, account, saved events, orders, and preferences pages now consume the backend API instead of `mockData`.
- Session state is stored locally with Zustand and sent as a bearer token to the backend.
