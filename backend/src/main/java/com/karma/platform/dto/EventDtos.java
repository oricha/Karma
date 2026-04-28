package com.karma.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class EventDtos {

    private EventDtos() {
    }

    public record EventResponse(
            String id,
            String organizerId,
            GroupDtos.OrganizerResponse organizer,
            String groupId,
            GroupDtos.GroupResponse group,
            String title,
            String slug,
            String description,
            String coverImageUrl,
            String startDate,
            String endDate,
            String venueName,
            String address,
            String city,
            String country,
            boolean isOnline,
            boolean isHybrid,
            String onlineUrl,
            String status,
            boolean featured,
            Integer maxAttendees,
            Integer currentAttendees,
            boolean isFree,
            Double price,
            String currency,
            String language,
            List<CatalogDtos.ThemeResponse> themes,
            CatalogDtos.CategoryResponse category,
            Double averageRating,
            Integer reviewCount
    ) {
    }

    public record EventDetailResponse(
            EventResponse event,
            List<EventResponse> relatedEvents,
            List<ReviewResponse> reviews
    ) {
    }

    public record RsvpResponse(
            String id,
            String eventId,
            String userId,
            String status,
            Integer waitlistPosition,
            boolean checkedIn
    ) {
    }

    public record ReviewAuthorResponse(
            String id,
            String firstName,
            String lastName,
            String avatarUrl
    ) {
    }

    public record ReviewResponse(
            String id,
            String eventId,
            String userId,
            ReviewAuthorResponse author,
            int rating,
            String comment,
            String createdAt,
            String updatedAt
    ) {
    }

    public record UpsertReviewRequest(
            @Min(1) @Max(5) int rating,
            @Size(max = 2000) String comment
    ) {
    }
}
