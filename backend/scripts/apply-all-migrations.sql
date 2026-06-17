-- Karma: apply all Flyway migrations manually (local PostgreSQL, no Docker)
-- 1) Connect to postgres DB and run: CREATE DATABASE karma_local;
-- 2) Connect to karma_local and run the rest of this file.

-- Karma Platform: enable PostGIS for geospatial queries (ST_DWithin, GIST indexes)
CREATE EXTENSION IF NOT EXISTS postgis;
-- Karma Platform: complete persistent domain schema (matches JPA entities)

-- ---------------------------------------------------------------------------
-- Authentication & users
-- ---------------------------------------------------------------------------

CREATE TABLE app_users (
    id              VARCHAR(64)  PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(120) NOT NULL,
    last_name       VARCHAR(120) NOT NULL,
    avatar_url      VARCHAR(1000),
    bio             VARCHAR(2000),
    phone           VARCHAR(64),
    role            VARCHAR(32)  NOT NULL,
    locale          VARCHAR(12)  NOT NULL,
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL
);

COMMENT ON TABLE app_users IS 'Registered platform users (attendees, organizers, admins)';

CREATE TABLE user_preferences (
    user_id               VARCHAR(64) PRIMARY KEY REFERENCES app_users(id) ON DELETE CASCADE,
    newsletter_frequency  VARCHAR(32) NOT NULL,
    review_reminders      BOOLEAN     NOT NULL DEFAULT TRUE,
    preferred_location    VARCHAR(255),
    latitude              DOUBLE PRECISION NOT NULL DEFAULT 0,
    longitude             DOUBLE PRECISION NOT NULL DEFAULT 0,
    location_radius_km    INTEGER     NOT NULL,
    created_at            TIMESTAMP   NOT NULL,
    updated_at            TIMESTAMP   NOT NULL
);

COMMENT ON TABLE user_preferences IS 'Discovery radius, newsletter frequency, and location preferences';

CREATE TABLE user_theme_preferences (
    user_id  VARCHAR(64) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    theme_id VARCHAR(64) NOT NULL,
    PRIMARY KEY (user_id, theme_id)
);

CREATE TABLE refresh_tokens (
    token      VARCHAR(1024) PRIMARY KEY,
    user_id    VARCHAR(64)   NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP     NOT NULL,
    created_at TIMESTAMP     NOT NULL
);

CREATE TABLE password_reset_tokens (
    token       VARCHAR(128) PRIMARY KEY,
    user_id     VARCHAR(64)  NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP    NOT NULL,
    used_at     TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL
);

CREATE TABLE email_verification_tokens (
    token       VARCHAR(128) PRIMARY KEY,
    user_id     VARCHAR(64)  NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP    NOT NULL,
    used_at     TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL
);

-- ---------------------------------------------------------------------------
-- Taxonomy: categories & themes
-- ---------------------------------------------------------------------------

CREATE TABLE categories (
    id             VARCHAR(64)  PRIMARY KEY,
    slug           VARCHAR(128) NOT NULL UNIQUE,
    name_es        VARCHAR(255) NOT NULL,
    name_en        VARCHAR(255) NOT NULL,
    description_es VARCHAR(1000),
    description_en VARCHAR(1000),
    image_url      VARCHAR(1000),
    event_count    INTEGER      NOT NULL DEFAULT 0,
    sort_order     INTEGER      NOT NULL DEFAULT 0,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL
);

CREATE TABLE themes (
    id          VARCHAR(64)  PRIMARY KEY,
    category_id VARCHAR(64)  NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name_es     VARCHAR(255) NOT NULL,
    name_en     VARCHAR(255) NOT NULL,
    slug        VARCHAR(128) NOT NULL UNIQUE,
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);

-- ---------------------------------------------------------------------------
-- Organizers & groups
-- ---------------------------------------------------------------------------

CREATE TABLE organizer_profiles (
    id         VARCHAR(64)  PRIMARY KEY,
    user_id    VARCHAR(64)  NOT NULL UNIQUE REFERENCES app_users(id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(128) NOT NULL UNIQUE,
    bio        VARCHAR(4000),
    website    VARCHAR(1000),
    logo_url   VARCHAR(1000),
    verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

CREATE TABLE community_groups (
    id           VARCHAR(64)  PRIMARY KEY,
    organizer_id VARCHAR(64)  NOT NULL REFERENCES organizer_profiles(id) ON DELETE CASCADE,
    name         VARCHAR(255) NOT NULL,
    slug         VARCHAR(128) NOT NULL UNIQUE,
    description  VARCHAR(4000),
    category_id  VARCHAR(64)  NOT NULL REFERENCES categories(id),
    banner_url   VARCHAR(1000),
    city         VARCHAR(128) NOT NULL,
    country      VARCHAR(128) NOT NULL,
    latitude     DOUBLE PRECISION NOT NULL DEFAULT 0,
    longitude    DOUBLE PRECISION NOT NULL DEFAULT 0,
    is_private   BOOLEAN      NOT NULL DEFAULT FALSE,
    status       VARCHAR(32)  NOT NULL,
    member_count INTEGER      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL
);

COMMENT ON TABLE community_groups IS 'Wellness communities that host events (Meetup-style groups)';

CREATE TABLE group_memberships (
    id                      VARCHAR(64) PRIMARY KEY,
    group_id                VARCHAR(64) NOT NULL REFERENCES community_groups(id) ON DELETE CASCADE,
    user_id                 VARCHAR(64) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    role                    VARCHAR(32) NOT NULL,
    status                  VARCHAR(32) NOT NULL,
    notification_preference VARCHAR(32) NOT NULL,
    joined_at               TIMESTAMP   NOT NULL,
    approved_at             TIMESTAMP,
    UNIQUE (group_id, user_id)
);

-- ---------------------------------------------------------------------------
-- Events, RSVPs & saved events
-- ---------------------------------------------------------------------------

CREATE TABLE events (
    id                VARCHAR(64)  PRIMARY KEY,
    organizer_id      VARCHAR(64)  NOT NULL REFERENCES organizer_profiles(id) ON DELETE CASCADE,
    group_id          VARCHAR(64)  REFERENCES community_groups(id) ON DELETE SET NULL,
    title             VARCHAR(255) NOT NULL,
    slug              VARCHAR(128) NOT NULL UNIQUE,
    description       VARCHAR(4000),
    cover_image_url   VARCHAR(1000),
    start_date        TIMESTAMP    NOT NULL,
    end_date          TIMESTAMP,
    venue_name        VARCHAR(255),
    address           VARCHAR(1000),
    city              VARCHAR(128) NOT NULL,
    country           VARCHAR(128) NOT NULL,
    latitude          DOUBLE PRECISION NOT NULL DEFAULT 0,
    longitude         DOUBLE PRECISION NOT NULL DEFAULT 0,
    is_online         BOOLEAN      NOT NULL DEFAULT FALSE,
    is_hybrid         BOOLEAN      NOT NULL DEFAULT FALSE,
    online_url        VARCHAR(1000),
    status            VARCHAR(32)  NOT NULL,
    featured          BOOLEAN      NOT NULL DEFAULT FALSE,
    max_attendees     INTEGER,
    is_free           BOOLEAN      NOT NULL DEFAULT TRUE,
    price             DOUBLE PRECISION,
    currency          VARCHAR(16),
    language          VARCHAR(12)  NOT NULL DEFAULT 'es',
    category_id       VARCHAR(64)  NOT NULL REFERENCES categories(id),
    reminders_enabled BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL
);

CREATE TABLE event_themes (
    event_id VARCHAR(64) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    theme_id VARCHAR(64) NOT NULL REFERENCES themes(id) ON DELETE CASCADE,
    PRIMARY KEY (event_id, theme_id)
);

CREATE TABLE rsvps (
    id                VARCHAR(64) PRIMARY KEY,
    event_id          VARCHAR(64) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id           VARCHAR(64) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    status            VARCHAR(32) NOT NULL,
    waitlist_position INTEGER,
    checked_in        BOOLEAN     NOT NULL DEFAULT FALSE,
    no_show           BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP   NOT NULL,
    updated_at        TIMESTAMP   NOT NULL,
    UNIQUE (event_id, user_id)
);

CREATE TABLE saved_events (
    id       VARCHAR(64) PRIMARY KEY,
    user_id  VARCHAR(64) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    event_id VARCHAR(64) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    saved_at TIMESTAMP   NOT NULL,
    UNIQUE (user_id, event_id)
);

-- ---------------------------------------------------------------------------
-- Ticketing & orders
-- ---------------------------------------------------------------------------

CREATE TABLE ticket_types (
    id                  VARCHAR(64) PRIMARY KEY,
    event_id            VARCHAR(64) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    name                VARCHAR(255) NOT NULL,
    description         VARCHAR(2000),
    price               DOUBLE PRECISION NOT NULL,
    currency            VARCHAR(16)      NOT NULL,
    quantity            INTEGER          NOT NULL,
    sold_count          INTEGER          NOT NULL DEFAULT 0,
    early_bird_price    DOUBLE PRECISION,
    early_bird_quantity INTEGER,
    early_bird_deadline TIMESTAMP,
    sale_start          TIMESTAMP,
    sale_end            TIMESTAMP,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL
);

CREATE TABLE event_orders (
    id                       VARCHAR(64) PRIMARY KEY,
    user_id                  VARCHAR(64) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    event_id                 VARCHAR(64) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    status                   VARCHAR(32) NOT NULL,
    total_amount             DOUBLE PRECISION NOT NULL,
    currency                 VARCHAR(16)      NOT NULL,
    purchased_at             TIMESTAMP        NOT NULL,
    stripe_session_id        VARCHAR(255),
    stripe_payment_intent_id VARCHAR(255),
    checkout_url             VARCHAR(2000),
    confirmed_at             TIMESTAMP
);

CREATE TABLE event_order_items (
    id             VARCHAR(64) PRIMARY KEY,
    event_order_id VARCHAR(64) NOT NULL REFERENCES event_orders(id) ON DELETE CASCADE,
    ticket_type_id VARCHAR(64) REFERENCES ticket_types(id) ON DELETE SET NULL,
    ticket_name    VARCHAR(255) NOT NULL,
    unit_price     DOUBLE PRECISION NOT NULL,
    currency       VARCHAR(16)      NOT NULL,
    quantity       INTEGER          NOT NULL,
    created_at     TIMESTAMP        NOT NULL,
    updated_at     TIMESTAMP        NOT NULL
);

-- ---------------------------------------------------------------------------
-- Reviews, discussions & blog
-- ---------------------------------------------------------------------------

CREATE TABLE reviews (
    id         VARCHAR(64) PRIMARY KEY,
    event_id   VARCHAR(64) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id    VARCHAR(64) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    rating     INTEGER     NOT NULL,
    comment    VARCHAR(2000),
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP   NOT NULL,
    UNIQUE (event_id, user_id)
);

CREATE TABLE group_posts (
    id         VARCHAR(64) PRIMARY KEY,
    group_id   VARCHAR(64) NOT NULL REFERENCES community_groups(id) ON DELETE CASCADE,
    author_id  VARCHAR(64) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    content    VARCHAR(4000) NOT NULL,
    image_url  VARCHAR(1000),
    is_pinned  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP   NOT NULL
);

CREATE TABLE group_post_replies (
    id         VARCHAR(64) PRIMARY KEY,
    post_id    VARCHAR(64) NOT NULL REFERENCES group_posts(id) ON DELETE CASCADE,
    author_id  VARCHAR(64) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    content    VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP   NOT NULL
);

CREATE TABLE blog_posts (
    id              VARCHAR(64)  PRIMARY KEY,
    title_es        VARCHAR(255) NOT NULL,
    title_en        VARCHAR(255) NOT NULL,
    title_ca        VARCHAR(255),
    slug            VARCHAR(128) NOT NULL UNIQUE,
    excerpt_es      VARCHAR(1000) NOT NULL,
    excerpt_en      VARCHAR(1000) NOT NULL,
    excerpt_ca      VARCHAR(1000),
    content_es      VARCHAR(20000),
    content_en      VARCHAR(20000),
    content_ca      VARCHAR(20000),
    cover_image_url VARCHAR(1000),
    featured        BOOLEAN      NOT NULL DEFAULT FALSE,
    published       BOOLEAN      NOT NULL DEFAULT TRUE,
    published_at    DATE,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL
);

COMMENT ON TABLE blog_posts IS 'Trilingual blog posts (ES/EN/CA) for platform news and newsletters';

-- ---------------------------------------------------------------------------
-- Email, digests, reminders & notifications
-- ---------------------------------------------------------------------------

CREATE TABLE email_digest_logs (
    id                   VARCHAR(64) PRIMARY KEY,
    user_id              VARCHAR(64) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    newsletter_frequency VARCHAR(32) NOT NULL,
    locale               VARCHAR(12) NOT NULL,
    status               VARCHAR(32) NOT NULL,
    last_digest_sent_at  TIMESTAMP,
    sent_at              TIMESTAMP,
    created_at           TIMESTAMP   NOT NULL,
    updated_at           TIMESTAMP   NOT NULL
);

CREATE TABLE event_reminder_logs (
    id            VARCHAR(64) PRIMARY KEY,
    event_id      VARCHAR(64) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id       VARCHAR(64) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    reminder_type VARCHAR(32) NOT NULL,
    locale        VARCHAR(12) NOT NULL,
    status        VARCHAR(32) NOT NULL,
    sent_at       TIMESTAMP,
    created_at    TIMESTAMP   NOT NULL,
    updated_at    TIMESTAMP   NOT NULL,
    UNIQUE (event_id, user_id, reminder_type)
);

CREATE TABLE event_review_request_logs (
    id         VARCHAR(64) PRIMARY KEY,
    event_id   VARCHAR(64) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id    VARCHAR(64) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    locale     VARCHAR(12) NOT NULL,
    status     VARCHAR(32) NOT NULL,
    sent_at    TIMESTAMP   NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (event_id, user_id)
);

CREATE TABLE email_daily_send_counter (
    send_date  DATE PRIMARY KEY,
    sent_count INTEGER   NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT TIMEZONE('utc', NOW())
);

CREATE TABLE email_deferred_send (
    id              VARCHAR(64)  PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    subject         VARCHAR(500) NOT NULL,
    html_body       TEXT         NOT NULL,
    priority        VARCHAR(32)  NOT NULL,
    scheduled_for   TIMESTAMP    NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT TIMEZONE('utc', NOW()),
    sent_at         TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- Indexes (query patterns + PostGIS GIST)
-- ---------------------------------------------------------------------------

CREATE INDEX idx_app_users_email ON app_users(email);
CREATE INDEX idx_events_start_date ON events(start_date);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_category_id ON events(category_id);
CREATE INDEX idx_events_organizer_id ON events(organizer_id);
CREATE INDEX idx_events_group_id ON events(group_id);
CREATE INDEX idx_rsvps_event_id ON rsvps(event_id);
CREATE INDEX idx_rsvps_user_id ON rsvps(user_id);
CREATE INDEX idx_group_memberships_group_id ON group_memberships(group_id);
CREATE INDEX idx_group_memberships_user_id ON group_memberships(user_id);
CREATE INDEX idx_ticket_types_event_id ON ticket_types(event_id);
CREATE INDEX idx_reviews_event_id ON reviews(event_id);
CREATE INDEX idx_group_posts_group_id ON group_posts(group_id);
CREATE INDEX idx_group_post_replies_post_id ON group_post_replies(post_id);
CREATE INDEX idx_email_digest_logs_user_id ON email_digest_logs(user_id);
CREATE INDEX idx_email_digest_logs_status_last_sent ON email_digest_logs(status, last_digest_sent_at);
CREATE INDEX idx_event_reminder_logs_event_user_type ON event_reminder_logs(event_id, user_id, reminder_type);
CREATE INDEX idx_event_review_request_logs_sent_at ON event_review_request_logs(sent_at);
CREATE UNIQUE INDEX idx_event_orders_stripe_session_id ON event_orders(stripe_session_id);
CREATE INDEX idx_event_order_items_order_id ON event_order_items(event_order_id);
CREATE INDEX idx_email_deferred_send_scheduled ON email_deferred_send(scheduled_for) WHERE sent_at IS NULL;
CREATE INDEX idx_blog_posts_slug ON blog_posts(slug);
CREATE INDEX idx_blog_posts_published ON blog_posts(published, published_at DESC);

CREATE INDEX idx_groups_geo ON community_groups
    USING GIST (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326));
CREATE INDEX idx_events_geo ON events
    USING GIST (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326));
CREATE INDEX idx_user_preferences_geo ON user_preferences
    USING GIST (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326));
insert into app_users (id, email, password_hash, first_name, last_name, avatar_url, bio, phone, role, locale, email_verified, created_at, updated_at) values
('user-1', 'maria@karma.app', '{noop}password123', 'Maria', 'Luna', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=face', 'Facilitadora de danza consciente y ceremonias de cacao.', '+34111111111', 'ORGANIZER', 'es', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('user-2', 'carlos@karma.app', '{noop}password123', 'Carlos', 'Sanchez', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face', 'Profesor de yoga y meditacion.', '+34222222222', 'ORGANIZER', 'es', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('user-3', 'demo@karma.app', '{noop}demo123', 'Demo', 'User', null, 'Explorando eventos conscientes.', '+34999999999', 'USER', 'es', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (id) do nothing;

insert into user_preferences (user_id, newsletter_frequency, review_reminders, preferred_location, latitude, longitude, location_radius_km, created_at, updated_at) values
('user-1', 'MONTHLY', true, 'Madrid', 40.4168, -3.7038, 30, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('user-2', 'KARMA_ONLY', false, 'Barcelona', 41.3874, 2.1686, 40, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('user-3', 'WEEKLY', true, 'Madrid', 40.4168, -3.7038, 50, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (user_id) do nothing;

insert into categories (id, slug, name_es, name_en, description_es, description_en, image_url, event_count, sort_order, created_at, updated_at) values
('cat-workshops', 'talleres', 'Talleres', 'Workshops', 'Talleres de bienestar', 'Wellness workshops', 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&h=300&fit=crop', 128, 1, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('cat-ceremonies', 'ceremonias', 'Ceremonias', 'Ceremonies', 'Ceremonias conscientes', 'Conscious ceremonies', 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&h=300&fit=crop', 56, 2, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('cat-dance', 'danza', 'Danza', 'Dance', 'Danza y movimiento', 'Dance and movement', 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=400&h=300&fit=crop', 89, 3, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('cat-music', 'musica', 'Musica', 'Music', 'Musica y vibracion', 'Music and vibration', 'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=400&h=300&fit=crop', 34, 4, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('cat-retreats', 'festivales-retiros', 'Festivales y Retiros', 'Festivals & Retreats', 'Experiencias inmersivas', 'Immersive experiences', 'https://images.unsplash.com/photo-1528495612343-9ca9f755e7bc?w=400&h=300&fit=crop', 22, 5, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (id) do nothing;

insert into themes (id, category_id, name_es, name_en, slug, sort_order, created_at, updated_at) values
('theme-yoga', 'cat-workshops', 'Yoga', 'Yoga', 'yoga', 1, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-meditation', 'cat-workshops', 'Meditacion', 'Meditation', 'meditacion', 2, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-ecstatic', 'cat-dance', 'Danza Extatica', 'Ecstatic Dance', 'danza-extatica', 3, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-tantra', 'cat-workshops', 'Tantra', 'Tantra', 'tantra', 4, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-cacao', 'cat-ceremonies', 'Cacao', 'Cacao', 'cacao', 5, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-kirtan', 'cat-music', 'Kirtan', 'Kirtan', 'kirtan', 6, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-sound', 'cat-music', 'Sound Healing', 'Sound Healing', 'sound-healing', 7, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-breathwork', 'cat-workshops', 'Breathwork', 'Breathwork', 'breathwork', 8, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (id) do nothing;

insert into user_theme_preferences (user_id, theme_id) values
('user-1', 'theme-cacao'),
('user-1', 'theme-ecstatic'),
('user-2', 'theme-yoga'),
('user-2', 'theme-meditation'),
('user-3', 'theme-yoga'),
('user-3', 'theme-ecstatic')
on conflict do nothing;

insert into organizer_profiles (id, user_id, name, slug, bio, website, logo_url, verified, created_at, updated_at) values
('org-1', 'user-1', 'Maria Luna', 'maria-luna', 'Facilitadora de danza consciente y ceremonias de cacao con mas de 10 anos de experiencia.', null, 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=face', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('org-2', 'user-2', 'Carlos Sanchez', 'carlos-sanchez', 'Profesor de yoga y meditacion.', null, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face', false, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (id) do nothing;

insert into community_groups (id, organizer_id, name, slug, description, category_id, banner_url, city, country, latitude, longitude, is_private, status, member_count, created_at, updated_at) values
('group-1', 'org-1', 'Ecstatic Dance Madrid', 'ecstatic-dance-madrid', 'Comunidad de danza libre y consciente en Madrid.', 'cat-dance', 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=800&h=300&fit=crop', 'Madrid', 'Espana', 40.4168, -3.7038, false, 'ACTIVE', 342, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('group-2', 'org-1', 'Yoga en el Parque BCN', 'yoga-parque-bcn', 'Sesiones de yoga gratuitas al aire libre en Barcelona.', 'cat-workshops', 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800&h=300&fit=crop', 'Barcelona', 'Espana', 41.3874, 2.1686, false, 'ACTIVE', 189, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('group-3', 'org-2', 'Circulo de Cacao Valencia', 'circulo-cacao-valencia', 'Ceremonias de cacao y circulos de compartir en Valencia.', 'cat-ceremonies', 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800&h=300&fit=crop', 'Valencia', 'Espana', 39.4699, -0.3763, false, 'ACTIVE', 98, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (id) do nothing;

insert into group_memberships (id, group_id, user_id, role, status, notification_preference, joined_at, approved_at) values
('membership-1', 'group-1', 'user-3', 'MEMBER', 'ACTIVE', 'IMMEDIATE', timestamp '2026-03-20 10:00:00', timestamp '2026-03-20 10:00:00'),
('membership-2', 'group-2', 'user-3', 'MEMBER', 'ACTIVE', 'DIGEST', timestamp '2026-03-21 10:00:00', timestamp '2026-03-21 10:00:00')
on conflict (id) do nothing;

insert into events (id, organizer_id, group_id, title, slug, description, cover_image_url, start_date, end_date, venue_name, address, city, country, latitude, longitude, is_online, is_hybrid, online_url, status, featured, max_attendees, is_free, price, currency, language, category_id, reminders_enabled, created_at, updated_at) values
('event-1', 'org-1', 'group-1', 'Danza Extatica al Atardecer', 'danza-extatica-atardecer', 'Una experiencia de movimiento libre y consciente acompanada de musica envolvente.', 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=600&h=400&fit=crop', timestamp '2026-04-05 18:00:00', timestamp '2026-04-05 21:00:00', 'Espacio Gaia', 'Calle del Sol 12', 'Madrid', 'Espana', 40.4168, -3.7038, false, false, null, 'PUBLISHED', true, 50, true, null, null, 'es', 'cat-dance', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('event-2', 'org-1', 'group-3', 'Ceremonia de Cacao Sagrado', 'ceremonia-cacao-sagrado', 'Circulo de cacao y musica medicina.', 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&h=400&fit=crop', timestamp '2026-04-08 19:00:00', timestamp '2026-04-08 22:00:00', 'Casa del Alma', null, 'Barcelona', 'Espana', 41.3874, 2.1686, false, false, null, 'PUBLISHED', false, 25, false, 35.0, 'EUR', 'es', 'cat-ceremonies', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('event-3', 'org-2', 'group-2', 'Retiro de Yoga y Meditacion', 'retiro-yoga-meditacion', 'Dos dias de practica, descanso y comunidad.', 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600&h=400&fit=crop', timestamp '2026-04-12 09:00:00', timestamp '2026-04-13 18:00:00', 'Finca La Paz', null, 'Granada', 'Espana', 37.1773, -3.5986, false, false, null, 'PUBLISHED', true, 20, false, 120.0, 'EUR', 'es', 'cat-retreats', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('event-4', 'org-2', null, 'Kirtan Cantos de Mantras', 'kirtan-cantos-mantras', 'Una noche para cantar y conectar.', 'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&h=400&fit=crop', timestamp '2026-04-06 20:00:00', timestamp '2026-04-06 22:30:00', 'Centro Ananda', null, 'Valencia', 'Espana', 39.4699, -0.3763, false, false, null, 'PUBLISHED', false, 40, true, null, null, 'es', 'cat-music', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('event-5', 'org-1', 'group-2', 'Taller de Trabajo de Respiracion', 'taller-breathwork', 'Practica guiada de respiracion consciente.', 'https://images.unsplash.com/photo-1528495612343-9ca9f755e7bc?w=600&h=400&fit=crop', timestamp '2026-04-10 10:00:00', timestamp '2026-04-10 13:00:00', 'Espacio Gaia', null, 'Madrid', 'Espana', 40.4168, -3.7038, false, false, null, 'PUBLISHED', false, 30, false, 25.0, 'EUR', 'es', 'cat-workshops', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('event-6', 'org-2', null, 'Meditacion Guiada Online', 'meditacion-guiada-online', 'Sesion online para empezar el dia centrado.', 'https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=600&h=400&fit=crop', timestamp '2026-04-07 08:00:00', timestamp '2026-04-07 09:00:00', null, null, 'Online', '', 0, 0, true, false, 'https://karma.app/live/meditacion', 'PUBLISHED', false, null, true, null, null, 'es', 'cat-workshops', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (id) do nothing;

insert into event_themes (event_id, theme_id) values
('event-1', 'theme-ecstatic'),
('event-2', 'theme-cacao'),
('event-3', 'theme-yoga'),
('event-3', 'theme-meditation'),
('event-4', 'theme-kirtan'),
('event-5', 'theme-breathwork'),
('event-6', 'theme-meditation')
on conflict do nothing;

insert into rsvps (id, event_id, user_id, status, waitlist_position, checked_in, no_show, created_at, updated_at) values
('rsvp-1', 'event-1', 'user-3', 'YES', null, false, false, timestamp '2026-03-25 10:00:00', timestamp '2026-03-25 10:00:00'),
('rsvp-2', 'event-6', 'user-3', 'YES', null, false, false, timestamp '2026-03-26 10:00:00', timestamp '2026-03-26 10:00:00')
on conflict (id) do nothing;

insert into saved_events (id, user_id, event_id, saved_at) values
('saved-1', 'user-3', 'event-2', timestamp '2026-03-29 10:00:00'),
('saved-2', 'user-3', 'event-3', timestamp '2026-03-30 10:00:00')
on conflict (id) do nothing;

insert into event_orders (id, user_id, event_id, status, total_amount, currency, purchased_at) values
('order-1', 'user-3', 'event-3', 'PAID', 120.0, 'EUR', timestamp '2026-03-15 10:00:00')
on conflict (id) do nothing;

insert into blog_posts (id, title_es, title_en, slug, excerpt_es, excerpt_en, cover_image_url, published_at, created_at, updated_at) values
('blog-1', '5 Beneficios de la Danza Extatica', '5 Benefits of Ecstatic Dance', '5-beneficios-danza-extatica', 'Descubre como la danza libre puede transformar tu bienestar fisico y emocional.', 'Discover how free dance can transform your physical and emotional well-being.', 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=400&h=250&fit=crop', date '2026-03-15', timestamp '2026-03-15 10:00:00', timestamp '2026-03-15 10:00:00'),
('blog-2', 'Guia para tu Primera Ceremonia de Cacao', 'Guide to Your First Cacao Ceremony', 'guia-primera-ceremonia-cacao', 'Todo lo que necesitas saber antes de asistir a una ceremonia de cacao sagrado.', 'Everything you need to know before attending a sacred cacao ceremony.', 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&h=250&fit=crop', date '2026-03-10', timestamp '2026-03-10 10:00:00', timestamp '2026-03-10 10:00:00')
on conflict (id) do nothing;
-- Sample events for July/August 2026 (extends base seed data)

INSERT INTO events (
    id, organizer_id, group_id, title, slug, description, cover_image_url,
    start_date, end_date, venue_name, address, city, country,
    latitude, longitude, is_online, is_hybrid, online_url,
    status, featured, max_attendees, is_free, price, currency, language,
    category_id, reminders_enabled, created_at, updated_at
) VALUES
(
    'event-july-1', 'org-1', 'group-1',
    'Sunset Ecstatic Dance', 'sunset-ecstatic-dance-july',
    'Sesion de danza consciente al atardecer con DJ set organico y cierre en silencio.',
    'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=600&h=400&fit=crop',
    TIMESTAMP '2026-07-11 19:00:00', TIMESTAMP '2026-07-11 22:00:00',
    'Espacio Matadero', NULL, 'Madrid', 'Espana', 40.4168, -3.7038,
    FALSE, FALSE, NULL, 'PUBLISHED', TRUE, 50, FALSE, 24.0, 'EUR', 'es',
    'cat-dance', TRUE, TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00'
),
(
    'event-july-2', 'org-2', 'group-2',
    'Urban Breathwork Lab', 'urban-breathwork-july',
    'Laboratorio guiado de respiracion, regulacion del sistema nervioso y journaling.',
    'https://images.unsplash.com/photo-1528495612343-9ca9f755e7bc?w=600&h=400&fit=crop',
    TIMESTAMP '2026-07-18 10:00:00', TIMESTAMP '2026-07-18 13:30:00',
    'Casa Virupa', NULL, 'Barcelona', 'Espana', 41.3874, 2.1686,
    FALSE, FALSE, NULL, 'PUBLISHED', FALSE, 30, FALSE, 32.0, 'EUR', 'es',
    'cat-workshops', TRUE, TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00'
),
(
    'event-july-3', 'org-2', NULL,
    'Mantra Night Online', 'mantra-night-july',
    'Circulo digital de canto y meditacion para comunidad hispanohablante.',
    'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&h=400&fit=crop',
    TIMESTAMP '2026-07-24 20:00:00', TIMESTAMP '2026-07-24 21:30:00',
    NULL, NULL, 'Online', '', 0, 0,
    TRUE, FALSE, 'https://karma.app/live/mantra-night', 'PUBLISHED', FALSE, NULL, TRUE, NULL, NULL, 'es',
    'cat-music', TRUE, TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00'
),
(
    'event-august-1', 'org-1', 'group-1',
    'Summer Embodiment Retreat', 'summer-embodiment-retreat-august',
    'Fin de semana inmersivo con yoga suave, danza, cacao y banos de sonido.',
    'https://images.unsplash.com/photo-1528495612343-9ca9f755e7bc?w=600&h=400&fit=crop',
    TIMESTAMP '2026-08-07 17:00:00', TIMESTAMP '2026-08-09 16:00:00',
    'Finca El Bosque', NULL, 'Sierra de Madrid', 'Espana', 40.4168, -3.7038,
    FALSE, FALSE, NULL, 'PUBLISHED', TRUE, 20, FALSE, 185.0, 'EUR', 'es',
    'cat-retreats', TRUE, TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00'
),
(
    'event-august-2', 'org-2', 'group-2',
    'Park Yoga Sunrise', 'park-yoga-sunrise-august',
    'Clase abierta al amanecer con meditacion breve y picnic comunitario.',
    'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600&h=400&fit=crop',
    TIMESTAMP '2026-08-16 07:30:00', TIMESTAMP '2026-08-16 09:00:00',
    'Parc de la Ciutadella', NULL, 'Barcelona', 'Espana', 41.3874, 2.1686,
    FALSE, FALSE, NULL, 'PUBLISHED', FALSE, 60, TRUE, NULL, NULL, 'es',
    'cat-workshops', TRUE, TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00'
),
(
    'event-august-3', 'org-1', NULL,
    'Sound Healing Portal', 'sound-healing-portal-august',
    'Viaje inmersivo con cuencos, voz y ambient meditation.',
    'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&h=400&fit=crop',
    TIMESTAMP '2026-08-28 19:30:00', TIMESTAMP '2026-08-28 21:30:00',
    'Lumen Studio', NULL, 'Valencia', 'Espana', 39.4699, -0.3763,
    FALSE, FALSE, NULL, 'PUBLISHED', FALSE, 40, FALSE, 28.0, 'EUR', 'es',
    'cat-music', TRUE, TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO event_themes (event_id, theme_id) VALUES
    ('event-july-1', 'theme-ecstatic'),
    ('event-july-2', 'theme-breathwork'),
    ('event-july-3', 'theme-kirtan'),
    ('event-august-1', 'theme-yoga'),
    ('event-august-1', 'theme-cacao'),
    ('event-august-2', 'theme-yoga'),
    ('event-august-2', 'theme-meditation'),
    ('event-august-3', 'theme-sound')
ON CONFLICT DO NOTHING;
