package com.karma.platform.model;

public record Rsvp(
        String id,
        String eventId,
        String userId,
        RsvpStatus status,
        Integer waitlistPosition,
        boolean checkedIn,
        boolean noShow
) {
}
