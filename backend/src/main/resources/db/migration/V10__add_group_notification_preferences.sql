-- V10__add_group_notification_preferences.sql
-- Add per-group notification preference to group_membership

CREATE TYPE group_notification_preference AS ENUM ('IMMEDIATE', 'DIGEST', 'NEVER');

ALTER TABLE group_membership ADD COLUMN notify_new_events group_notification_preference NOT NULL DEFAULT 'DIGEST';

-- Index for efficient notification query filtering
CREATE INDEX idx_group_membership_notify_new_events ON group_membership(notify_new_events);

-- Comment
COMMENT ON COLUMN group_membership.notify_new_events IS 'User preference for notifications about new events in this group: IMMEDIATE, DIGEST, or NEVER';
