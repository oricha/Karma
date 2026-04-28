create table if not exists ticket_types (
    id varchar(64) primary key,
    event_id varchar(64) not null references events(id) on delete cascade,
    name varchar(255) not null,
    description varchar(2000),
    price double precision not null,
    currency varchar(16) not null,
    quantity integer not null,
    sold_count integer not null default 0,
    early_bird_price double precision,
    early_bird_quantity integer,
    early_bird_deadline timestamp,
    sale_start timestamp,
    sale_end timestamp,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists reviews (
    id varchar(64) primary key,
    event_id varchar(64) not null references events(id) on delete cascade,
    user_id varchar(64) not null references app_users(id) on delete cascade,
    rating integer not null,
    comment varchar(2000),
    created_at timestamp not null,
    updated_at timestamp not null,
    unique (event_id, user_id)
);

create table if not exists group_posts (
    id varchar(64) primary key,
    group_id varchar(64) not null references community_groups(id) on delete cascade,
    author_id varchar(64) not null references app_users(id) on delete cascade,
    content varchar(4000) not null,
    image_url varchar(1000),
    is_pinned boolean not null default false,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists group_post_replies (
    id varchar(64) primary key,
    post_id varchar(64) not null references group_posts(id) on delete cascade,
    author_id varchar(64) not null references app_users(id) on delete cascade,
    content varchar(4000) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists email_digest_logs (
    id varchar(64) primary key,
    user_id varchar(64) not null references app_users(id) on delete cascade,
    newsletter_frequency varchar(32) not null,
    locale varchar(12) not null,
    status varchar(32) not null,
    last_digest_sent_at timestamp,
    sent_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists event_reminder_logs (
    id varchar(64) primary key,
    event_id varchar(64) not null references events(id) on delete cascade,
    user_id varchar(64) not null references app_users(id) on delete cascade,
    reminder_type varchar(32) not null,
    locale varchar(12) not null,
    status varchar(32) not null,
    sent_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null,
    unique (event_id, user_id, reminder_type)
);

create index if not exists idx_ticket_types_event_id on ticket_types(event_id);
create index if not exists idx_reviews_event_id on reviews(event_id);
create index if not exists idx_group_posts_group_id on group_posts(group_id);
create index if not exists idx_group_post_replies_post_id on group_post_replies(post_id);
create index if not exists idx_email_digest_logs_user_id on email_digest_logs(user_id);
create index if not exists idx_email_digest_logs_status_last_sent on email_digest_logs(status, last_digest_sent_at);
create index if not exists idx_event_reminder_logs_event_user_type on event_reminder_logs(event_id, user_id, reminder_type);
