package com.karma.platform.dto;

public final class BlogDtos {

    private BlogDtos() {
    }

    public record BlogPostResponse(
            String id,
            String titleEs,
            String titleEn,
            String slug,
            String excerptEs,
            String excerptEn,
            String coverImageUrl,
            String publishedAt
    ) {
    }
}
