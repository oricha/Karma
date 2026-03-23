package com.karma.platform.model;

public record Category(
        String id,
        String nameEs,
        String nameEn,
        String slug,
        String descriptionEs,
        String descriptionEn,
        String imageUrl,
        int eventCount
) {
}
