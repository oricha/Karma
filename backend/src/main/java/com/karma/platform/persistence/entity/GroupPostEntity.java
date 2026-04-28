package com.karma.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_posts")
public class GroupPostEntity extends AuditableEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "group_id", nullable = false, length = 64)
    private String groupId;

    @Column(name = "author_id", nullable = false, length = 64)
    private String authorId;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}
