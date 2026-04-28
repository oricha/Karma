package com.karma.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_post_replies")
public class GroupPostReplyEntity extends AuditableEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "post_id", nullable = false, length = 64)
    private String postId;

    @Column(name = "author_id", nullable = false, length = 64)
    private String authorId;

    @Column(nullable = false, length = 4000)
    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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
}
