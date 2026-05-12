package com.karma.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
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
            boolean checkedIn,
            boolean noShow
    ) {
    }

    public record UpsertEventRequest(
            String groupId,
            @NotBlank @Size(max = 255) String title,
            @Size(max = 4000) String description,
            String coverImageUrl,
            @NotNull LocalDateTime startDate,
            LocalDateTime endDate,
            @Size(max = 255) String venueName,
            @Size(max = 1000) String address,
            @NotBlank @Size(max = 128) String city,
            @NotBlank @Size(max = 128) String country,
            Double latitude,
            Double longitude,
            boolean isOnline,
            boolean isHybrid,
            String onlineUrl,
            String status,
            boolean featured,
            @Min(1) Integer maxAttendees,
            boolean isFree,
            @Min(0) Double price,
            String currency,
            @NotBlank @Size(max = 12) String language,
            @NotBlank String categoryId,
            List<String> themeIds,
            Boolean remindersEnabled
    ) {
    }

    public record AttendanceUpdateRequest(
            @NotBlank String userId
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
