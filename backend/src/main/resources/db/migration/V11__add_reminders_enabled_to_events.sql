-- V11__add_reminders_enabled_to_events.sql
-- Add reminders control flag to events table

ALTER TABLE events ADD COLUMN reminders_enabled BOOLEAN NOT NULL DEFAULT true;

-- Comment
COMMENT ON COLUMN events.reminders_enabled IS 'Event organizer can disable automated reminders (7-day, 1-day, 2-hour) for this event';
