-- Phase 5: SendGrid free-tier daily quota tracking and deferred sends

create table if not exists email_daily_send_counter (
    send_date date primary key,
    sent_count integer not null default 0,
    updated_at timestamp not null default timezone('utc', now())
);

create table if not exists email_deferred_send (
    id varchar(64) primary key,
    recipient_email varchar(255) not null,
    subject varchar(500) not null,
    html_body text not null,
    priority varchar(32) not null,
    scheduled_for timestamp not null,
    created_at timestamp not null default timezone('utc', now()),
    sent_at timestamp
);

create index if not exists idx_email_deferred_send_scheduled
    on email_deferred_send (scheduled_for)
    where sent_at is null;
