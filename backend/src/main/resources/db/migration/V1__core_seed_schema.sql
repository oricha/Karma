create table if not exists category_seed (
    id varchar(64) primary key,
    slug varchar(128) not null unique,
    name_es varchar(255) not null,
    name_en varchar(255) not null
);

create table if not exists organizer_seed (
    id varchar(64) primary key,
    slug varchar(128) not null unique,
    name varchar(255) not null,
    city varchar(128)
);

create table if not exists group_seed (
    id varchar(64) primary key,
    organizer_id varchar(64) not null,
    category_id varchar(64) not null,
    slug varchar(128) not null unique,
    name varchar(255) not null,
    city varchar(128) not null,
    country varchar(128) not null,
    is_private boolean not null default false,
    member_count integer not null default 0,
    constraint fk_group_seed_organizer foreign key (organizer_id) references organizer_seed(id),
    constraint fk_group_seed_category foreign key (category_id) references category_seed(id)
);

create table if not exists event_seed (
    id varchar(64) primary key,
    organizer_id varchar(64) not null,
    group_id varchar(64),
    category_id varchar(64) not null,
    slug varchar(128) not null unique,
    title varchar(255) not null,
    description varchar(2000),
    city varchar(128) not null,
    country varchar(128) not null,
    venue_name varchar(255),
    start_date timestamp not null,
    end_date timestamp not null,
    is_online boolean not null default false,
    is_hybrid boolean not null default false,
    is_free boolean not null default true,
    price decimal(10,2),
    currency varchar(16),
    status varchar(32) not null,
    reminders_enabled boolean not null default true,
    constraint fk_event_seed_organizer foreign key (organizer_id) references organizer_seed(id),
    constraint fk_event_seed_group foreign key (group_id) references group_seed(id),
    constraint fk_event_seed_category foreign key (category_id) references category_seed(id)
);

insert into category_seed (id, slug, name_es, name_en) values
    ('cat-workshops', 'talleres', 'Talleres', 'Workshops'),
    ('cat-dance', 'danza', 'Danza', 'Dance'),
    ('cat-retreats', 'festivales-retiros', 'Festivales y Retiros', 'Festivals and Retreats'),
    ('cat-music', 'musica', 'Música', 'Music')
on conflict (id) do nothing;

insert into organizer_seed (id, slug, name, city) values
    ('org-1', 'maria-luna', 'María Luna', 'Madrid'),
    ('org-2', 'carlos-sanchez', 'Carlos Sánchez', 'Barcelona')
on conflict (id) do nothing;

insert into group_seed (id, organizer_id, category_id, slug, name, city, country, is_private, member_count) values
    ('group-1', 'org-1', 'cat-dance', 'ecstatic-dance-madrid', 'Ecstatic Dance Madrid', 'Madrid', 'España', false, 342),
    ('group-2', 'org-2', 'cat-workshops', 'yoga-parque-bcn', 'Yoga en el Parque BCN', 'Barcelona', 'España', false, 189)
on conflict (id) do nothing;
