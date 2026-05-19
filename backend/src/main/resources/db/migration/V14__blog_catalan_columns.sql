-- Phase 5: trilingual blog (Catalan columns)

alter table if exists blog_posts add column if not exists title_ca varchar(255);
alter table if exists blog_posts add column if not exists excerpt_ca varchar(1000);
alter table if exists blog_posts add column if not exists content_ca varchar(20000);

update blog_posts
set title_ca = coalesce(title_ca, title_es),
    excerpt_ca = coalesce(excerpt_ca, excerpt_es),
    content_ca = coalesce(content_ca, content_es)
where title_ca is null or excerpt_ca is null;
