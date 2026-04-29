package com.karma.platform.dto;

import java.util.List;

public final class GroupDtos {

    private GroupDtos() {
    }

    public record OrganizerResponse(
            String id,
            String userId,
            String name,
            String slug,
            String bio,
            String website,
            String logoUrl,
            boolean verified
    ) {
    }

    public record GroupResponse(
            String id,
            String organizerId,
            OrganizerResponse organizer,
            String name,
            String slug,
            String description,
            String categoryId,
            CatalogDtos.CategoryResponse category,
            String bannerUrl,
            String city,
            String country,
            boolean isPrivate,
            String status,
            int memberCount,
            int upcomingEventCount,
            String notificationPreference
    ) {
    }

    public record GroupDetailResponse(
            GroupResponse group,
            List<EventDtos.EventResponse> upcomingEvents,
            List<UserDtos.UserResponse> members,
            int postCount,
            String lastPostAt
    ) {
    }

    public record UpsertGroupRequest(
            String name,
            String description,
            String categoryId,
            String bannerUrl,
            String city,
            String country,
            boolean isPrivate
    ) {
    }

    public record MembershipResponse(
            String id,
            String groupId,
            String userId,
            UserDtos.UserResponse user,
            String role,
            String status,
            String notificationPreference,
            String joinedAt,
            String approvedAt
    ) {
    }

    public record GroupPostAuthorResponse(
            String id,
            String firstName,
            String lastName,
            String avatarUrl
    ) {
    }

    public record GroupPostReplyResponse(
            String id,
            String postId,
            GroupPostAuthorResponse author,
            String content,
            String createdAt
    ) {
    }

    public record GroupPostResponse(
            String id,
            String groupId,
            GroupPostAuthorResponse author,
            String content,
            String imageUrl,
            boolean pinned,
            int replyCount,
            String createdAt,
            String updatedAt,
            List<GroupPostReplyResponse> replies
    ) {
    }

    public record UpsertGroupPostRequest(
            String content,
            String imageUrl
    ) {
    }

    public record UpsertGroupPostReplyRequest(
            String content
    ) {
    }
}
