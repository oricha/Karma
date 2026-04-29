alter table if exists blog_posts add column if not exists content_es varchar(20000);
alter table if exists blog_posts add column if not exists content_en varchar(20000);
alter table if exists blog_posts add column if not exists featured boolean not null default false;
alter table if exists blog_posts add column if not exists published boolean not null default true;

update blog_posts
set content_es = coalesce(content_es, excerpt_es),
    content_en = coalesce(content_en, excerpt_en),
    featured = coalesce(featured, false),
    published = coalesce(published, true);
