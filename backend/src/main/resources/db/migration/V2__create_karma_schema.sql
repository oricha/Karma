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
