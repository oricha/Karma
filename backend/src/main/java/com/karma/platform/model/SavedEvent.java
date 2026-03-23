package com.karma.platform.model;

import java.time.LocalDateTime;

public record SavedEvent(
        String id,
        String userId,
        String eventId,
        LocalDateTime savedAt
) {
}
