package com.karma.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blog_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title_es", nullable = false, length = 255)
    private String titleEs;

    @Column(name = "title_en", nullable = false, length = 255)
    private String titleEn;

    @Column(name = "excerpt_es", nullable = false, columnDefinition = "TEXT")
    private String excerptEs;

    @Column(name = "excerpt_en", nullable = false, columnDefinition = "TEXT")
    private String excerptEn;

    @Column(name = "content_es", nullable = false, columnDefinition = "TEXT")
    private String contentEs;

    @Column(name = "content_en", nullable = false, columnDefinition = "TEXT")
    private String contentEn;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "featured", nullable = false)
    private Boolean featured = false;

    @Column(name = "published", nullable = false)
    private Boolean published = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
