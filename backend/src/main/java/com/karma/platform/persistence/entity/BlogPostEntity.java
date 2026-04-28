package com.karma.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "blog_posts")
public class BlogPostEntity extends AuditableEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "title_es", nullable = false, length = 255)
    private String titleEs;

    @Column(name = "title_en", nullable = false, length = 255)
    private String titleEn;

    @Column(nullable = false, unique = true, length = 128)
    private String slug;

    @Column(name = "excerpt_es", nullable = false, length = 1000)
    private String excerptEs;

    @Column(name = "excerpt_en", nullable = false, length = 1000)
    private String excerptEn;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @Column(name = "published_at", nullable = false)
    private LocalDate publishedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitleEs() {
        return titleEs;
    }

    public void setTitleEs(String titleEs) {
        this.titleEs = titleEs;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getExcerptEs() {
        return excerptEs;
    }

    public void setExcerptEs(String excerptEs) {
        this.excerptEs = excerptEs;
    }

    public String getExcerptEn() {
        return excerptEn;
    }

    public void setExcerptEn(String excerptEn) {
        this.excerptEn = excerptEn;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public LocalDate getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDate publishedAt) {
        this.publishedAt = publishedAt;
    }
}
