package com.karma.platform.dto;

public final class BlogDtos {

    private BlogDtos() {
    }

    public record BlogPostResponse(
            String id,
            String titleEs,
            String titleEn,
            String titleCa,
            String slug,
            String excerptEs,
            String excerptEn,
            String excerptCa,
            String contentEs,
            String contentEn,
            String contentCa,
            String coverImageUrl,
            boolean featured,
            boolean published,
            String publishedAt
    ) {
    }

    public record UpsertBlogPostRequest(
            String titleEs,
            String titleEn,
            String titleCa,
            String excerptEs,
            String excerptEn,
            String excerptCa,
            String contentEs,
            String contentEn,
            String contentCa,
            String coverImageUrl,
            boolean featured,
            boolean published
    ) {
    }
}
