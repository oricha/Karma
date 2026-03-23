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
            List<UserDtos.UserResponse> members
    ) {
    }
}
