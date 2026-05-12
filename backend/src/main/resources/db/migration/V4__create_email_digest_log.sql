-- V4__create_email_digest_log.sql
-- Create table to track weekly digest sends and prevent duplicates

CREATE TABLE email_digest_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    locale VARCHAR(5) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SENT', -- SENT, PENDING, FAILED
    unsubscribed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for efficient queries
CREATE INDEX idx_email_digest_log_user_id ON email_digest_log(user_id);
CREATE INDEX idx_email_digest_log_sent_at ON email_digest_log(sent_at);
CREATE INDEX idx_email_digest_log_status ON email_digest_log(status);
CREATE INDEX idx_email_digest_log_user_sent ON email_digest_log(user_id, sent_at DESC);

-- Comment
COMMENT ON TABLE email_digest_log IS 'Tracks all weekly digest email sends to prevent duplicates and enforce frequency limits';
