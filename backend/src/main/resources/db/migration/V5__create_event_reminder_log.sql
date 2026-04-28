-- V5__create_event_reminder_log.sql
-- Create table to track event reminder sends and prevent duplicates

CREATE TYPE reminder_type AS ENUM ('7DAY', '1DAY', '2HOUR');

CREATE TABLE event_reminder_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reminder_type reminder_type NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    locale VARCHAR(5) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SENT', -- SENT, PENDING, FAILED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for efficient queries
CREATE INDEX idx_event_reminder_log_event_id ON event_reminder_log(event_id);
CREATE INDEX idx_event_reminder_log_user_id ON event_reminder_log(user_id);
CREATE INDEX idx_event_reminder_log_sent_at ON event_reminder_log(sent_at);
CREATE INDEX idx_event_reminder_log_status ON event_reminder_log(status);
CREATE INDEX idx_event_reminder_log_dedup ON event_reminder_log(event_id, user_id, reminder_type);

-- Comment
COMMENT ON TABLE event_reminder_log IS 'Tracks all event reminder sends (7-day, 1-day, 2-hour) to prevent duplicates and enforce frequency limits';
COMMENT ON TYPE reminder_type IS 'Reminder timing: 7DAY (7 days before), 1DAY (1 day before), 2HOUR (2 hours before)';
