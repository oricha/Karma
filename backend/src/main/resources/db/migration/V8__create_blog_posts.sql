-- V8__create_blog_posts.sql
-- Create table for bilingual blog posts

CREATE TABLE blog_posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title_es VARCHAR(255) NOT NULL,
    title_en VARCHAR(255) NOT NULL,
    excerpt_es TEXT NOT NULL,
    excerpt_en TEXT NOT NULL,
    content_es TEXT NOT NULL,
    content_en TEXT NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    cover_image_url VARCHAR(500),
    featured BOOLEAN NOT NULL DEFAULT false,
    published BOOLEAN NOT NULL DEFAULT false,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for efficient queries
CREATE INDEX idx_blog_posts_slug ON blog_posts(slug);
CREATE INDEX idx_blog_posts_published ON blog_posts(published);
CREATE INDEX idx_blog_posts_published_date ON blog_posts(published, published_at DESC);
CREATE INDEX idx_blog_posts_featured ON blog_posts(featured, published);

-- Comment
COMMENT ON TABLE blog_posts IS 'Bilingual blog posts (Spanish/English) for platform news and announcements; only published posts are visible to users';
