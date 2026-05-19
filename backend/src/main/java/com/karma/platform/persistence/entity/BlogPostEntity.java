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

    @Column(name = "title_ca", length = 255)
    private String titleCa;

    @Column(nullable = false, unique = true, length = 128)
    private String slug;

    @Column(name = "excerpt_es", nullable = false, length = 1000)
    private String excerptEs;

    @Column(name = "excerpt_en", nullable = false, length = 1000)
    private String excerptEn;

    @Column(name = "excerpt_ca", length = 1000)
    private String excerptCa;

    @Column(name = "content_es", length = 20000)
    private String contentEs;

    @Column(name = "content_en", length = 20000)
    private String contentEn;

    @Column(name = "content_ca", length = 20000)
    private String contentCa;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @Column(nullable = false)
    private boolean featured;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "published_at")
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

    public String getTitleCa() {
        return titleCa;
    }

    public void setTitleCa(String titleCa) {
        this.titleCa = titleCa;
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

    public String getExcerptCa() {
        return excerptCa;
    }

    public void setExcerptCa(String excerptCa) {
        this.excerptCa = excerptCa;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getContentEs() {
        return contentEs;
    }

    public void setContentEs(String contentEs) {
        this.contentEs = contentEs;
    }

    public String getContentEn() {
        return contentEn;
    }

    public void setContentEn(String contentEn) {
        this.contentEn = contentEn;
    }

    public String getContentCa() {
        return contentCa;
    }

    public void setContentCa(String contentCa) {
        this.contentCa = contentCa;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public LocalDate getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDate publishedAt) {
        this.publishedAt = publishedAt;
    }
}
