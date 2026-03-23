package com.karma.platform.model;

import java.time.LocalDate;

public record BlogPost(
        String id,
        String titleEs,
        String titleEn,
        String slug,
        String excerptEs,
        String excerptEn,
        String coverImageUrl,
        LocalDate publishedAt
) {
}
