create table if not exists app_users (
    id varchar(64) primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    first_name varchar(120) not null,
    last_name varchar(120) not null,
    avatar_url varchar(1000),
    bio varchar(2000),
    phone varchar(64),
    role varchar(32) not null,
    locale varchar(12) not null,
    email_verified boolean not null default false,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists user_preferences (
    user_id varchar(64) primary key references app_users(id) on delete cascade,
    newsletter_frequency varchar(32) not null,
    review_reminders boolean not null default true,
    preferred_location varchar(255),
    latitude double precision not null default 0,
    longitude double precision not null default 0,
    location_radius_km integer not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists user_theme_preferences (
    user_id varchar(64) not null references app_users(id) on delete cascade,
    theme_id varchar(64) not null,
    primary key (user_id, theme_id)
);

create table if not exists categories (
    id varchar(64) primary key,
    slug varchar(128) not null unique,
    name_es varchar(255) not null,
    name_en varchar(255) not null,
    description_es varchar(1000),
    description_en varchar(1000),
    image_url varchar(1000),
    event_count integer not null default 0,
    sort_order integer not null default 0,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists themes (
    id varchar(64) primary key,
    category_id varchar(64) not null references categories(id) on delete cascade,
    name_es varchar(255) not null,
    name_en varchar(255) not null,
    slug varchar(128) not null unique,
    sort_order integer not null default 0,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists organizer_profiles (
    id varchar(64) primary key,
    user_id varchar(64) not null unique references app_users(id) on delete cascade,
    name varchar(255) not null,
    slug varchar(128) not null unique,
    bio varchar(4000),
    website varchar(1000),
    logo_url varchar(1000),
    verified boolean not null default false,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists community_groups (
    id varchar(64) primary key,
    organizer_id varchar(64) not null references organizer_profiles(id) on delete cascade,
    name varchar(255) not null,
    slug varchar(128) not null unique,
    description varchar(4000),
    category_id varchar(64) not null references categories(id),
    banner_url varchar(1000),
    city varchar(128) not null,
    country varchar(128) not null,
    latitude double precision not null default 0,
    longitude double precision not null default 0,
    is_private boolean not null default false,
    status varchar(32) not null,
    member_count integer not null default 0,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists group_memberships (
    id varchar(64) primary key,
    group_id varchar(64) not null references community_groups(id) on delete cascade,
    user_id varchar(64) not null references app_users(id) on delete cascade,
    role varchar(32) not null,
    status varchar(32) not null,
    notification_preference varchar(32) not null,
    joined_at timestamp not null,
    approved_at timestamp,
    unique (group_id, user_id)
);

create table if not exists events (
    id varchar(64) primary key,
    organizer_id varchar(64) not null references organizer_profiles(id) on delete cascade,
    group_id varchar(64) references community_groups(id) on delete set null,
    title varchar(255) not null,
    slug varchar(128) not null unique,
    description varchar(4000),
    cover_image_url varchar(1000),
    start_date timestamp not null,
    end_date timestamp,
    venue_name varchar(255),
    address varchar(1000),
    city varchar(128) not null,
    country varchar(128) not null,
    latitude double precision not null default 0,
    longitude double precision not null default 0,
    is_online boolean not null default false,
    is_hybrid boolean not null default false,
    online_url varchar(1000),
    status varchar(32) not null,
    featured boolean not null default false,
    max_attendees integer,
    is_free boolean not null default true,
    price double precision,
    currency varchar(16),
    language varchar(12) not null default 'es',
    category_id varchar(64) not null references categories(id),
    reminders_enabled boolean not null default true,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists event_themes (
    event_id varchar(64) not null references events(id) on delete cascade,
    theme_id varchar(64) not null references themes(id) on delete cascade,
    primary key (event_id, theme_id)
);

create table if not exists rsvps (
    id varchar(64) primary key,
    event_id varchar(64) not null references events(id) on delete cascade,
    user_id varchar(64) not null references app_users(id) on delete cascade,
    status varchar(32) not null,
    waitlist_position integer,
    checked_in boolean not null default false,
    no_show boolean not null default false,
    created_at timestamp not null,
    updated_at timestamp not null,
    unique (event_id, user_id)
);

create table if not exists saved_events (
    id varchar(64) primary key,
    user_id varchar(64) not null references app_users(id) on delete cascade,
    event_id varchar(64) not null references events(id) on delete cascade,
    saved_at timestamp not null,
    unique (user_id, event_id)
);

create table if not exists event_orders (
    id varchar(64) primary key,
    user_id varchar(64) not null references app_users(id) on delete cascade,
    event_id varchar(64) not null references events(id) on delete cascade,
    status varchar(32) not null,
    total_amount double precision not null,
    currency varchar(16) not null,
    purchased_at timestamp not null
);

create table if not exists blog_posts (
    id varchar(64) primary key,
    title_es varchar(255) not null,
    title_en varchar(255) not null,
    slug varchar(128) not null unique,
    excerpt_es varchar(1000) not null,
    excerpt_en varchar(1000) not null,
    cover_image_url varchar(1000),
    published_at date not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists refresh_tokens (
    token varchar(1024) primary key,
    user_id varchar(64) not null references app_users(id) on delete cascade,
    expires_at timestamp not null,
    created_at timestamp not null
);

create table if not exists password_reset_tokens (
    token varchar(128) primary key,
    user_id varchar(64) not null references app_users(id) on delete cascade,
    expiry_date timestamp not null,
    used_at timestamp,
    created_at timestamp not null
);

create table if not exists email_verification_tokens (
    token varchar(128) primary key,
    user_id varchar(64) not null references app_users(id) on delete cascade,
    expiry_date timestamp not null,
    used_at timestamp,
    created_at timestamp not null
);

create index if not exists idx_groups_geo_expr on community_groups using gist (st_setsrid(st_makepoint(longitude, latitude), 4326));
create index if not exists idx_events_geo_expr on events using gist (st_setsrid(st_makepoint(longitude, latitude), 4326));
create index if not exists idx_user_preferences_geo_expr on user_preferences using gist (st_setsrid(st_makepoint(longitude, latitude), 4326));
