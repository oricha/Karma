package com.karma.platform.persistence.entity;

import com.karma.platform.model.GroupNotificationPreference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_memberships")
public class GroupMembershipEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "group_id", nullable = false, length = 64)
    private String groupId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(nullable = false, length = 32)
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_preference", nullable = false, length = 32)
    private GroupNotificationPreference notificationPreference;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GroupNotificationPreference getNotificationPreference() {
        return notificationPreference;
    }

    public void setNotificationPreference(GroupNotificationPreference notificationPreference) {
        this.notificationPreference = notificationPreference;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}
