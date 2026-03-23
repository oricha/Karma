package com.karma.platform.model;

public record Group(
        String id,
        String organizerId,
        String name,
        String slug,
        String description,
        String categoryId,
        String bannerUrl,
        String city,
        String country,
        double latitude,
        double longitude,
        boolean isPrivate,
        GroupStatus status,
        int memberCount
) {
}
