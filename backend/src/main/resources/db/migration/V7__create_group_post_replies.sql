-- V7__create_group_post_replies.sql
-- Create table for replies to group discussion posts

CREATE TABLE group_post_replies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES group_posts(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for efficient queries
CREATE INDEX idx_group_post_replies_post_id ON group_post_replies(post_id);
CREATE INDEX idx_group_post_replies_author_id ON group_post_replies(author_id);
CREATE INDEX idx_group_post_replies_created_at ON group_post_replies(created_at);

-- Comment
COMMENT ON TABLE group_post_replies IS 'Nested replies to group discussion posts; deleted when parent post is deleted';
