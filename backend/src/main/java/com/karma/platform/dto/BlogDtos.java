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
            String contentEs,
            String contentEn,
            String coverImageUrl,
            boolean featured,
            boolean published,
            String publishedAt
    ) {
    }

    public record UpsertBlogPostRequest(
            String titleEs,
            String titleEn,
            String excerptEs,
            String excerptEn,
            String contentEs,
            String contentEn,
            String coverImageUrl,
            boolean featured,
            boolean published
    ) {
    }
}
