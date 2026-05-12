-- V9__add_notification_preferences_to_users.sql
-- Add newsletter and notification preference columns to user_preferences

CREATE TYPE newsletter_frequency AS ENUM ('WEEKLY', 'BIWEEKLY', 'MONTHLY', 'NEVER', 'KARMA_ONLY');

ALTER TABLE user_preferences ADD COLUMN newsletter_freq newsletter_frequency NOT NULL DEFAULT 'WEEKLY';
ALTER TABLE user_preferences ADD COLUMN review_reminders BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE user_preferences ADD COLUMN last_digest_sent TIMESTAMP;

-- Index for efficient frequency enforcement query
CREATE INDEX idx_user_preferences_newsletter_freq ON user_preferences(newsletter_freq);

-- Comment
COMMENT ON COLUMN user_preferences.newsletter_freq IS 'User preference for digest frequency: WEEKLY, BIWEEKLY, MONTHLY, NEVER, or KARMA_ONLY (blog posts only)';
COMMENT ON COLUMN user_preferences.review_reminders IS 'User preference for review request emails after attended events';
COMMENT ON COLUMN user_preferences.last_digest_sent IS 'Timestamp of last sent digest for enforcing BIWEEKLY and MONTHLY frequencies';
