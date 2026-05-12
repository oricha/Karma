package com.karma.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "themes")
public class ThemeEntity extends AuditableEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "category_id", nullable = false, length = 64)
    private String categoryId;

    @Column(name = "name_es", nullable = false, length = 255)
    private String nameEs;

    @Column(name = "name_en", nullable = false, length = 255)
    private String nameEn;

    @Column(nullable = false, unique = true, length = 128)
    private String slug;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getNameEs() {
        return nameEs;
    }

    public void setNameEs(String nameEs) {
        this.nameEs = nameEs;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
