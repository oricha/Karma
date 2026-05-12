alter table if exists event_orders add column if not exists stripe_session_id varchar(255);
alter table if exists event_orders add column if not exists stripe_payment_intent_id varchar(255);
alter table if exists event_orders add column if not exists checkout_url varchar(2000);
alter table if exists event_orders add column if not exists confirmed_at timestamp;

create table if not exists event_order_items (
    id varchar(64) primary key,
    event_order_id varchar(64) not null references event_orders(id) on delete cascade,
    ticket_type_id varchar(64) references ticket_types(id) on delete set null,
    ticket_name varchar(255) not null,
    unit_price double precision not null,
    currency varchar(16) not null,
    quantity integer not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create unique index if not exists idx_event_orders_stripe_session_id on event_orders(stripe_session_id);
create index if not exists idx_event_order_items_order_id on event_order_items(event_order_id);
