package com.karma.platform.model;

public record GroupMembership(
        String id,
        String groupId,
        String userId,
        String role,
        String status,
        GroupNotificationPreference notificationPreference
) {
}
