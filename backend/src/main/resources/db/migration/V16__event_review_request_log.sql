create table if not exists event_review_request_logs (
    id varchar(64) primary key,
    event_id varchar(64) not null,
    user_id varchar(64) not null,
    locale varchar(12) not null,
    status varchar(32) not null,
    sent_at timestamp not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    constraint uq_event_review_request_logs_event_user unique (event_id, user_id)
);

create index if not exists idx_event_review_request_logs_sent_at on event_review_request_logs (sent_at);
