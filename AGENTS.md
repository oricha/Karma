# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

**Karma Platform** is a wellness and spirituality events portal built with a group-centric model similar to Meetup.com. Users discover and join wellness communities (groups), RSVP to free events, purchase tickets for paid events, and receive personalized event recommendations. Organizers create groups, host events, and manage their communities with analytics dashboards.

### Key Features (Full Platform Scope)

- **User Authentication**: Secure JWT-based authentication with password reset and email verification
- **Group Management**: Create groups by topic, manage members, private/public workflows
- **Event Discovery**: Location-based search with PostGIS, category/theme filtering, text search
- **RSVP System**: Free events with automatic waitlist promotion when spots open
- **Ticketing & Payments**: Paid events with Stripe integration, early-bird pricing support
- **Email Campaigns**: Weekly digests with personalized event recommendations, event reminders (7-day, 1-day, 2-hour)
- **Bilingual Support**: Spanish (primary) and English (secondary) throughout UI and emails
- **Reviews & Ratings**: Event attendees rate and review experiences
- **Organizer Dashboard**: Analytics showing RSVPs, revenue, attendance rates, activity trends
- **Recurring Events**: Auto-generate series (weekly, biweekly, monthly) up to 6 months ahead
- **Saved Events**: Bookmark events for later discovery
- **Discussion Forums**: Group members discuss between events with pinned posts
- **Blog & Content**: Admin-managed bilingual blog posts with featured content for newsletter blocks

### Technology Stack

- **Frontend**: React 18 + TypeScript + Vite + Tailwind CSS + shadcn/ui (Vercel)
- **Backend**: Java 21 + Spring Boot 3.x + Spring Data JPA + Spring Security (Docker container)
- **Database**: PostgreSQL 16 + PostGIS extension (Docker container)
- **Email**: SendGrid (transactional + bulk newsletters with rate limiting)
- **Payments**: Stripe (checkout sessions + webhooks + refunds)
- **Authentication**: JWT tokens (access: 15 min, refresh: 7 days)
- **File Storage**: S3-compatible (avatars, group banners, event covers, blog images)
- **Geospatial**: PostGIS `ST_DWithin` for radius queries, `geography(Point, 4326)` for accurate Earth calculations
- **Scheduling**: Spring `@Scheduler` for digest jobs, reminder jobs, email batching
- **Deployment**: Docker Compose (test/production), GitHub Actions (Dokploy + AWS EC2)

### Directory Structure

```
karma/
├── backend/                           # Java Spring Boot API server
│   ├── src/main/java/com/karma/      # Application code (20+ modules: auth, event, rsvp, group, etc.)
│   ├── src/main/resources/
│   │   ├── application.yml           # Base config
│   │   ├── application-local.yml     # Local PostgreSQL dev profile
│   │   ├── application-test.yml      # Docker test profile (Dokploy)
│   │   ├── application-production.yml# Docker production profile (AWS)
│   │   ├── messages.properties       # Spanish i18n strings
│   │   ├── messages_en.properties    # English i18n strings
│   │   └── db/migration/             # Flyway SQL migrations (seed schema + sample data)
│   ├── src/test/java/               # Spring Boot integration + unit tests
│   ├── Dockerfile                    # Multi-stage build (Gradle → Java 21 Alpine)
│   ├── build.gradle                  # Gradle config (Spring Boot 3.x, JPA, PostGIS, SendGrid, Stripe, etc.)
│   └── README.md                     # Backend commands + secrets + API surface
│
├── frontend/                          # React TypeScript SPA
│   ├── src/
│   │   ├── pages/                   # Route-level components (EventList, EventDetail, GroupDetail, Auth, Dashboard, etc.)
│   │   ├── components/              # Reusable UI (EventCard, GroupMembership, RSVPForm, Checkout, etc.)
│   │   ├── services/                # API client (axios instance with JWT interceptors, refresh token flow)
│   │   ├── store/                   # Zustand state management (auth session, user preferences)
│   │   ├── hooks/                   # Custom React hooks (useAuth, useEventSearch, usePreferences, etc.)
│   │   ├── utils/                   # Helpers (formatters, validators, geocoding, i18n)
│   │   ├── i18n/                    # Translation resources (Spanish + English)
│   │   └── main.tsx                 # App entry point with React Router + i18n setup
│   ├── vite.config.ts               # Vite bundler with API proxy to http://localhost:8081
│   ├── package.json                 # npm dependencies (React 18, Vite, TailwindCSS, shadcn/ui, axios, zustand, etc.)
│   └── README.md                     # Frontend commands + environment
│
├── .agent/specs/karma-platform/      # Complete product specification (read-only reference)
│   ├── requirements.md              # 30 detailed acceptance criteria (functional requirements)
│   ├── design.md                    # Architecture, entities, algorithms, ER diagrams, indexes
│   ├── tasks.md                     # 26-task implementation plan (bottom-up: infra → domain → API)
│   └── .config.kiro                # Kiro CLI config (OpenSpec generation)
│
├── deploy/                           # Deployment configuration
│   ├── test/docker-compose.yml      # Dokploy test stack (backend, PostgreSQL)
│   ├── production/docker-compose.yml# AWS EC2 production stack
│   └── Dockerfile                   # (deprecated, use backend/Dockerfile)
│
├── .github/workflows/               # CI/CD pipelines
│   ├── test-dokploy.yml            # Auto-deploy to Dokploy on push to develop
│   └── production-aws.yml           # Manual trigger: build, push to AWS ECR, deploy to EC2
│
└── AGENTS.md                         # This file
```

## Development Setup

### Prerequisites

- **Java 21**: Backend development
- **Node.js 18+**: Frontend development
- **PostgreSQL 16 + PostGIS**: `brew install postgresql@16 postgis` (macOS) or use Docker
- **Docker & Docker Compose**: For containerized stack (optional, use `local` profile for direct PostgreSQL)

### Local Database (Quick Start)

Option 1: Direct PostgreSQL (fastest for local dev):
```bash
# Install PostgreSQL 16 with PostGIS
brew install postgresql@16 postgis

# Create local database and enable PostGIS
brew services start postgresql@16
createdb karma_local
psql karma_local -c "CREATE EXTENSION postgis;"

# Flyway auto-runs on backend startup and seeds schema + sample events
```

Option 2: Docker Compose (isolated):
```bash
docker-compose -f deploy/test/docker-compose.yml up -d
# Backend connects via TCP to postgres:5432 (test profile)
```

### Running the Full Stack (Local)

**Terminal 1 - Backend:**
```bash
cd backend

# Option A: PostgreSQL installed on machine (recommended for dev iteration)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/karma_local
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
./gradlew bootRun -Plocal
# API: http://localhost:8081

# Option B: Docker PostgreSQL (test profile)
docker-compose -f ../deploy/test/docker-compose.yml up -d
./gradlew bootRun -Ptest
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm install
npm run dev
# UI: http://localhost:5173
# Vite proxy forwards /api/* to http://localhost:8081
```

### Common Commands

#### Backend (Spring Boot)

```bash
cd backend

# Run full test suite
./gradlew test

# Run single test
./gradlew test --tests "com.karma.auth.AuthServiceTest"

# Run integration tests only
./gradlew test -k "Integration"

# Build JAR
./gradlew build

# Start with local PostgreSQL profile
./gradlew bootRun -Plocal

# Clean build artifacts
./gradlew clean

# Check dependency updates
./gradlew dependencyUpdates

# Format code (if Spotless configured)
./gradlew spotlessApply
```

#### Frontend (React/Vite)

```bash
cd frontend

# Install dependencies
npm install

# Development server (hot reload)
npm run dev

# Build for production
npm run build

# Preview production build locally
npm run preview

# Run tests (if Jest configured)
npm run test

# Lint with ESLint
npm run lint

# Format code with Prettier
npm run format
```

#### Database Management

```bash
# Connect to local PostgreSQL
psql karma_local

# View tables and schema
\dt
\d rsvps
\d events

# Check PostGIS installation
SELECT PostGIS_Version();

# Test geospatial query (sample: events near 40.7128, -74.0060 within 10km)
SELECT e.title, ST_Distance(e.location_point, ST_MakePoint(-74.0060, 40.7128)::geography) / 1000 AS distance_km
FROM events e
WHERE ST_DWithin(e.location_point, ST_MakePoint(-74.0060, 40.7128)::geography, 10000)
ORDER BY distance_km ASC;

# Reset database (caution!)
DROP DATABASE karma_local;
CREATE DATABASE karma_local;
psql karma_local -c "CREATE EXTENSION postgis;"
# Flyway recreates schema on next backend startup
```

#### Docker Operations

```bash
# Start test stack (backend + PostgreSQL)
docker-compose -f deploy/test/docker-compose.yml up -d

# View logs
docker-compose -f deploy/test/docker-compose.yml logs -f backend

# Stop stack
docker-compose -f deploy/test/docker-compose.yml down

# Clean volumes (⚠️ deletes data)
docker-compose -f deploy/test/docker-compose.yml down -v
```

## Architecture & Key Patterns

### Backend Architecture (Domain-Driven Design)

The backend is organized into **20+ independent modules**, each handling a specific domain:

- **auth**: JWT token generation, password reset, email verification
- **user**: User profiles, avatar uploads, preference management
- **event**: Event CRUD, recurring generation, cancellation with notifications
- **rsvp**: RSVP creation/updates, waitlist position tracking
- **waitlist**: Auto-promotion algorithm when spots open
- **group**: Group CRUD, membership workflows (public/private), member roles
- **discussion**: Group discussion posts, replies, pinning (max 3 per group)
- **category**: Categories/themes taxonomy (seed data: Talleres, Ceremonias, Danza, etc.)
- **order**: Ticket orders, order items, refunds
- **payment**: Stripe checkout session creation, payment confirmation, webhook handling
- **preference**: User theme preferences, location radius, newsletter frequency (WEEKLY, BIWEEKLY, MONTHLY, NEVER, KARMA_ONLY)
- **savedevent**: Bookmarked events for later discovery
- **review**: Event reviews (1-5 rating), attendance validation, duplicate prevention
- **blog**: Bilingual blog posts (seed data, published status, featured content)
- **organizer**: Organizer profiles, dashboard with analytics (revenue, RSVP trends, attendance rates)
- **notification**: Email service (SendGrid), digest scheduling, reminder jobs (7-day, 1-day, 2-hour)
- **common**: Shared utilities, exception handling, i18n support, validators
- **config**: Security (JWT, CORS), Flyway database migrations, caching, logging

#### Key Service Interfaces (Overview)

Each module exports public service interfaces that handle business logic:

- `EventService`: findNearbyEvents (PostGIS ST_DWithin), findEvents (filters + sorting), publishEvent, cancelEvent, generateRecurringInstances
- `RsvpService`: createOrUpdateRsvp (auto-waitlist if full), cancelRsvp, checkInAttendee
- `WaitlistService`: addToWaitlist, promoteFromWaitlist (auto-triggered on RSVP cancel)
- `GroupService`: createGroup, joinGroup (public instant / private approval), leaveGroup, findNearbyGroups
- `EmailService`: sendWelcomeEmail, sendRsvpConfirmation, sendWaitlistPromotionEmail, sendWeeklyDigest, sendEventReminders
- `PaymentService`: createCheckoutSession (Stripe), confirmPayment (webhook verification), processRefund
- `DigestService`: generateDigestForUser (personalized from groups + theme matches + popular regional events)
- `OrganizerDashboardService`: getDashboardStats (counts + revenue + trends)

#### REST API Layer

All modules expose REST endpoints (typically in `YourDomainController`):

- **Public endpoints** (no auth): event/group listings, blog, categories
- **Protected endpoints** (JWT required): profile, preferences, RSVP, group membership, orders
- **Organizer endpoints** (role check): event/group creation, attendee management, dashboard, refunds
- **Admin endpoints** (ADMIN role): blog management

#### Repository Pattern

Each entity has a corresponding Spring Data `JpaRepository`:

```java
// Example: RsvpRepository
public interface RsvpRepository extends JpaRepository<Rsvp, UUID> {
    Optional<Rsvp> findByEventIdAndUserId(UUID eventId, UUID userId);
    List<Rsvp> findByEventIdAndStatus(UUID eventId, RsvpStatus status);
    Optional<Rsvp> findFirstByEventIdAndStatusOrderByWaitlistPositionAsc(UUID eventId, RsvpStatus status);
    int countByEventIdAndStatus(UUID eventId, RsvpStatus status);
}
```

Custom queries use `@Query` for complex logic (e.g., PostGIS ST_DWithin for nearby events).

### Frontend Architecture (React + Zustand)

#### Routing Structure

Routes are organized by feature:

- `/` → Home (event discovery)
- `/events/:slug` → Event detail (RSVP/ticket form)
- `/groups` → Group discovery
- `/groups/:slug` → Group detail (join/leave, discussion posts)
- `/categories/:slug` → Events by category
- `/blog` → Blog listing
- `/blog/:slug` → Blog post
- `/auth/login` → Login form
- `/auth/register` → Registration form
- `/auth/reset-password/:token` → Password reset
- `/account` → User profile, preferences (theme, newsletter freq, location radius)
- `/account/saved-events` → Bookmarked events
- `/account/orders` → Purchased tickets
- `/organizer/register` → Become organizer
- `/organizer/dashboard` → Analytics (RSVPs, revenue, trends)
- `/organizer/events` → Event management (create, edit, publish)
- `/organizer/groups` → Group management
- `/orders/:id` → Order confirmation (after Stripe checkout)

#### State Management (Zustand)

A single global store (`src/store/index.ts` or similar) manages:

```typescript
// Authentication
- user (User profile + JWT token)
- isAuthenticated
- setUser, clearUser, refreshToken

// UI
- locale (es / en)
- setLocale (persists to localStorage for anonymous, backend for logged-in)

// User Preferences
- preferences (theme preferences, newsletter freq, location, location radius)
- setPreferences
```

JWT tokens are stored in `localStorage` and added to all API requests via axios interceptor.

#### API Client Service

`src/services/api.ts` (or similar) exports:

```typescript
// Axios instance with JWT interceptor + refresh token logic
const api = axios.create({
  baseURL: VITE_API_URL || 'http://localhost:8081',
})

// Before every request: attach JWT from localStorage
api.interceptors.request.use(config => {
  const token = localStorage.getItem('access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// If 401, try refresh token; if that fails, logout
api.interceptors.response.use(
  res => res,
  async error => {
    if (error.response.status === 401) {
      // attempt token refresh via POST /api/auth/refresh
      // if success, retry original request
      // if fail, redirect to login
    }
  }
)

export const eventApi = {
  list: (filters) => api.get('/events', { params: filters }),
  getBySlug: (slug) => api.get(`/events/${slug}`),
  nearby: (lat, lng, radius) => api.get('/events/nearby', { params: { lat, lng, radius } }),
  rsvp: (eventId, status) => api.post(`/events/${eventId}/rsvp`, { status }),
  ...
}

export const groupApi = { ... }
export const authApi = { ... }
// etc.
```

#### Component Hierarchy

- `src/pages/`: Route-level components (fetch data, compose lower components)
- `src/components/`: Reusable UI (EventCard, GroupCard, RSVPForm, CheckoutForm, etc.)
- Tailwind CSS classes for styling; shadcn/ui for accessible form elements

### Geospatial Queries (PostGIS)

Karma heavily uses PostGIS for location-based discovery:

```sql
-- Find events within radius
SELECT e.*, ST_Distance(e.location_point, ST_MakePoint(?, ?)::geography) / 1000 AS distance_km
FROM events e
WHERE ST_DWithin(e.location_point, ST_MakePoint(?, ?)::geography, ? * 1000)
  AND e.status = 'PUBLISHED'
  AND e.start_date >= NOW()
ORDER BY distance_km ASC;

-- Find groups near user's location
SELECT g.*, ST_Distance(g.location_point, up.location_point) / 1000 AS distance_km
FROM groups g, user_preferences up
WHERE up.user_id = ?
  AND ST_DWithin(g.location_point, up.location_point, up.location_radius_km * 1000)
ORDER BY distance_km ASC;
```

Backend repository queries use Spring Data's native queries:

```java
@Query(value = 
  "SELECT e FROM Event e WHERE ST_DWithin(e.locationPoint, ST_MakePoint(?1, ?2), ?3 * 1000) AND e.status = 'PUBLISHED'",
  nativeQuery = true)
List<Event> findNearbyEvents(Double lng, Double lat, Integer radiusKm);
```

### Key Algorithms

#### Waitlist Auto-Promotion

When a user cancels their RSVP (YES → NO):

1. Update RSVP to NO
2. Query first waitlisted user: `findFirstByEventIdAndStatusOrderByWaitlistPositionAsc`
3. Update their status to YES, clear waitlist_position
4. Decrement waitlist positions for remaining waitlisted users
5. Send "You're in!" email notification

#### Weekly Digest Generation

Runs every Monday at 9:00 AM UTC (Spring Scheduler):

1. Query users by `newsletter_freq` (filter by WEEKLY, BIWEEKLY, MONTHLY frequency; skip NEVER)
2. For each user:
   - Query **events from joined groups** (upcoming, published) → limit 5
   - Query **events matching theme preferences** within `location_radius_km` → limit 5
   - Query **popular events** (by RSVP count) in user's region → limit 3
3. Render bilingual email template (user's preferred locale)
4. Send via SendGrid with rate limiting (max 500/hour)
5. Log to `email_digest_log` table

For users with `KARMA_ONLY` frequency: send platform-news-only digest (featured blog posts, announcements).

#### Recurring Event Generation

When organizer creates recurring event:

1. Validate `recurrence_end <= 6 months from start_date`
2. Create parent event with `event_type = RECURRING`
3. Loop from `start_date` to `recurrence_end`:
   - If `recurrence_rule = WEEKLY`: increment by 7 days
   - If `BIWEEKLY`: increment by 14 days
   - If `MONTHLY`: same day of next month
   - Create child event: copy all fields, set `start_date = current_date`, set `parent_event_id`, generate unique slug

On update/cancel: apply changes only to future instances (where `start_date > NOW()`).

### Database Schema Highlights

Key design decisions:

- **UUIDs for PKs**: Security + distributed systems
- **PostGIS geography(Point, 4326)**: Accurate Earth distance calculations
- **Denormalized counters**: `group.memberCount`, `ticketType.soldCount` (updated on membership/order changes)
- **Composite unique constraints**: Prevent duplicate RSVPs, group memberships, saved events, reviews
- **JSONB for flexibility**: Email payloads, notification preferences (future extensibility)
- **Cascading deletes**: Maintain referential integrity (e.g., deleting group cascades to memberships, events, posts)
- **Indexes on query patterns**: GiST on location_point, B-Tree on start_date, status, foreign keys, email

See `.agent/specs/karma-platform/design.md` (Database Schema section) for full ER diagram and index definitions.

## Implementation Status

See `.agent/specs/karma-platform/tasks.md` for the complete 26-task roadmap. Current highlights:

### ✅ Completed
- Project infrastructure (Spring Boot, Gradle, Flyway migrations)
- Core domain entities (User, Event, Rsvp, Group, Order, etc.)
- JWT authentication (register, login, refresh, password reset, email verification)
- Event CRUD + recurring generation
- RSVP system + waitlist logic
- Group management + membership workflows
- Most REST API endpoints (event/group discovery, auth, user profile, preferences)
- Frontend integration with backend API (React → axios client)
- Category/theme taxonomy (seed data)
- Blog system (bilingual content)
- Organizer profiles + dashboard (basic analytics)

### 🚧 In Progress / Remaining
- Full email service integration (SendGrid transactional + digest scheduling)
- Weekly digest job + reminder jobs (7-day, 1-day, 2-hour)
- Event cancellation notifications + organizer attendee operations
- Reviews + ratings system
- Stripe payment webhooks + refunds
- Geospatial search (findNearbyEvents, findNearbyGroups) optimization
- Discussion forums (group posts/replies)
- Frontend organizer dashboard (event analytics, attendee management)
- Performance testing (load test event listing, digest generation at scale)
- Security hardening (rate limiting, input sanitization, HTTPS)
- Docker deployment (test/production stacks verified)

Refer to tasks.md for task-level details, acceptance criteria, and requirement traceability.

## Development Workflow

### Adding a New Feature

1. **Identify scope**: Check `.agent/specs/karma-platform/requirements.md` for related acceptance criteria
2. **Entity creation**: Define JPA entity + repository in appropriate module
3. **Service layer**: Create `YourDomainService` interface + implementation
4. **REST controller**: Expose endpoints in `YourDomainController`
5. **Tests**: Write unit tests (MockMvc for controllers) + integration tests
6. **Frontend**: Create pages/components consuming the API
7. **Database**: Add Flyway migration if schema changes
8. **Documentation**: Update task status in tasks.md

### Testing Strategy

- **Unit tests**: Mock dependencies, test service logic
- **Integration tests**: Use `@SpringBootTest` with `TestRestTemplate` or `MockMvc`
- **Database tests**: Use in-memory H2 or containerized PostgreSQL via Testcontainers
- **Geospatial tests**: Verify PostGIS queries with sample coordinates
- **Email tests**: Mock SendGrid client to avoid sending real emails
- **Payment tests**: Use Stripe test mode API keys

### Code Organization Best Practices

- **One domain = one package**: `com.karma.event`, `com.karma.rsvp`, `com.karma.group`, etc.
- **DTO for API contracts**: Never expose entities directly; map with MapStruct or manual DTOs
- **Service interfaces**: Define contracts; implementations handle business logic
- **Exception handling**: Use custom exceptions; `@ControllerAdvice` returns localized error responses
- **Validation**: Use Hibernate Validator `@NotNull`, `@Email`, etc. + custom validators
- **Caching**: Use `@Cacheable` for categories, themes, popular events
- **Transactions**: Default `@Transactional` on service methods; override scope as needed

## Internationalization (i18n)

The platform supports Spanish (default) and English everywhere:

**Backend:**
- `src/main/resources/messages.properties` (Spanish)
- `src/main/resources/messages_en.properties` (English)
- `LocaleResolver` extracts locale from `Accept-Language` header or user preference
- Services fetch localized strings via `messageSource.getMessage("key.name", args, locale)`
- Email templates include locale-specific templates (e.g., `welcome-es.html`, `welcome-en.html`)

**Frontend:**
- `src/i18n/` contains translation JSON files
- Language switcher sets locale in Zustand store
- Routes include locale prefix: `/es/eventos`, `/en/events`
- `hreflang` meta tags for SEO

New features must support both languages from the start.

## Common Issues & Troubleshooting

### Backend

**Issue**: PostgreSQL connection error at startup
- **Fix**: Ensure `SPRING_DATASOURCE_URL`, `USERNAME`, `PASSWORD` are set or PostgreSQL is running
- **Check**: `psql karma_local -c "SELECT 1"` to verify DB connection

**Issue**: Flyway migration failed
- **Cause**: Usually old migration files or schema conflicts
- **Fix**: `DROP DATABASE karma_local; createdb karma_local; psql karma_local -c "CREATE EXTENSION postgis;"`
- Then restart backend to re-run migrations

**Issue**: PostGIS extension not found
- **Fix**: `psql karma_local -c "CREATE EXTENSION postgis;"`
- **Verify**: `SELECT PostGIS_Version();`

**Issue**: JWT token invalid / 401 Unauthorized
- **Check**: Token expiry (access tokens: 15 min)
- **Frontend** must refresh token via POST `/api/auth/refresh` before expiry
- **Verify**: `JWT_SECRET` in `application.yml` matches across environments

### Frontend

**Issue**: API calls return 404 or connection refused
- **Check**: Backend is running on http://localhost:8081
- **Check**: Vite proxy config in `vite.config.ts` targets correct backend URL
- **Override**: Set `VITE_API_URL=http://localhost:8081` in `.env.local`

**Issue**: Locale not persisting
- **Frontend**: Anonymous users store in `localStorage` via Zustand
- **Logged-in users**: Stored in backend `user_preferences.locale`
- **Check**: Browser console → Application → Local Storage for `locale` key

**Issue**: CORS errors
- **Frontend origin** must be in backend `CORS_ALLOWED_ORIGINS`
- **Development**: `localhost:5173` should be allowed (check `application.yml`)

## Deployment & CI/CD

### Local Docker Stack

```bash
# Start test stack (backend + PostgreSQL)
docker-compose -f deploy/test/docker-compose.yml up --build

# Logs
docker-compose -f deploy/test/docker-compose.yml logs -f backend
```

### GitHub Actions Workflows

**`.github/workflows/test-dokploy.yml`** (auto-trigger on push to `develop`):
- Build backend Docker image
- Push to Dokploy registry
- Trigger Dokploy deployment webhook

**`.github/workflows/production-aws.yml`** (manual trigger):
- Build + push to AWS ECR
- SSH into EC2 instance
- Run docker-compose with production stack
- Requires AWS + GitHub secrets

### Environment-Specific Config

Each Spring profile has its own `application-{profile}.yml`:

- **local**: Direct PostgreSQL on machine, debug logging
- **test**: Docker PostgreSQL (Dokploy), test SendGrid key
- **production**: Production PostgreSQL, production Stripe/SendGrid/S3 keys

Critical secrets (JWT_SECRET, DB passwords, API keys) stored in GitHub Actions secrets and injected at deploy time, never committed to repo.

## Performance Considerations

### Backend

- **Pagination**: All list endpoints use Pageable (default page size: 20)
- **Connection pooling**: HikariCP with default pool size 10
- **Caching**: @Cacheable on CategoryService.getAllCategories(), theme queries
- **Database indexes**: GiST on location_point, B-Tree on start_date, status, foreign keys
- **Response time**: <500ms 95th percentile for event listing (requirement 27)

### Frontend

- **Code splitting**: Route-based lazy loading with React.lazy
- **Image optimization**: Use responsive image sizes, WebP format
- **Bundle size**: Monitor with `npm run build` output
- **Load time**: <2s on 3G (requirement 21)

## Security Notes

- **Passwords**: Hashed with BCrypt before storage; never logged or exposed in responses
- **JWTs**: Store in `localStorage`; always sent with Bearer prefix in Authorization header
- **CORS**: Restricted to frontend origin only
- **SQL Injection**: Use parameterized queries (Spring Data JPA, no string concatenation)
- **XSS**: Sanitize user input (descriptions, posts, comments); React auto-escapes by default
- **HTTPS**: Enforced in production (nginx reverse proxy or AWS ALB termination)
- **Rate limiting**: 100 requests/minute per IP (requirement 28)
- **OWASP Top 10**: Input validation, CSRF tokens (Spring Security built-in), no hardcoded secrets

## Useful References

- **API Spec**: Backend README at `backend/README.md` lists all implemented endpoints
- **Requirements**: Full acceptance criteria at `.agent/specs/karma-platform/requirements.md` (30 requirements)
- **Design**: Architecture, entities, algorithms at `.agent/specs/karma-platform/design.md`
- **Implementation Plan**: Task-by-task breakdown at `.agent/specs/karma-platform/tasks.md` (26 tasks + traceability)
- **Spring Boot Docs**: https://docs.spring.io/spring-boot/
- **React Docs**: https://react.dev/
- **PostGIS Docs**: https://postgis.net/
- **Stripe Integration**: https://stripe.com/docs (checkout sessions, webhooks, refunds)
- **SendGrid Integration**: https://sendgrid.com/docs (transactional email + bulk marketing)

## Next Steps for New Contributors

1. Read this file fully (you are here ✓)
2. Run the local dev stack (backend + frontend)
3. Test a flow: register → discover events → RSVP → check database
4. Pick a task from `.agent/specs/karma-platform/tasks.md` (focus on incomplete, high-priority items)
5. Create a feature branch: `git checkout -b feature/task-description`
6. Implement entity/service/controller/tests following the patterns above
7. Test in browser, verify database state, run test suite
8. Commit with descriptive message + task reference
9. Push to GitHub; CI/CD validates + deploys to Dokploy/AWS

Welcome to Karma Platform! 🙏
