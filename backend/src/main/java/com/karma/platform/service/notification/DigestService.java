package com.karma.platform.service.notification;

import com.karma.platform.entity.User;

public interface DigestService {

    /**
     * Generate personalized digest for a user combining:
     * - Events from user's joined groups (next 14 days, max 5)
     * - Events matching user's theme preferences and location radius (max 5)
     * - Popular regional events by RSVP count (max 3)
     */
    EmailService.DigestContent generateDigestForUser(User user);

    /**
     * Send digest to user with rate limiting and logging
     */
    void sendDigestToUser(User user);

    /**
     * Update last_digest_sent timestamp for frequency enforcement
     */
    void recordDigestSent(User user);
}
