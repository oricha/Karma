package com.karma.platform.dto;

import java.util.List;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record CategoryResponse(
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

    public record ThemeResponse(
            String id,
            String categoryId,
            String nameEs,
            String nameEn,
            String slug
    ) {
    }

    public record CategoryDetailsResponse(
            CategoryResponse category,
            List<ThemeResponse> themes
    ) {
    }
}
