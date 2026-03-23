package com.karma.platform.model;

public record OrganizerProfile(
        String id,
        String userId,
        String name,
        String slug,
        String bio,
        String website,
        String logoUrl,
        boolean verified
) {
}
