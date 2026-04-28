-- V6__create_group_posts.sql
-- Create table for group discussion posts

CREATE TABLE group_posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    is_pinned BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for efficient queries
CREATE INDEX idx_group_posts_group_id ON group_posts(group_id);
CREATE INDEX idx_group_posts_author_id ON group_posts(author_id);
CREATE INDEX idx_group_posts_is_pinned ON group_posts(is_pinned);
CREATE INDEX idx_group_posts_sort ON group_posts(group_id, is_pinned DESC, created_at DESC);

-- Comment
COMMENT ON TABLE group_posts IS 'Discussion posts created by group members; visible only to group members';
