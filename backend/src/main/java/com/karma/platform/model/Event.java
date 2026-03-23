package com.karma.platform.model;

import java.time.LocalDateTime;
import java.util.List;

public record Event(
        String id,
        String organizerId,
        String groupId,
        String title,
        String slug,
        String description,
        String coverImageUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String venueName,
        String address,
        String city,
        String country,
        double latitude,
        double longitude,
        boolean isOnline,
        boolean isHybrid,
        String onlineUrl,
        EventStatus status,
        boolean featured,
        Integer maxAttendees,
        boolean isFree,
        Double price,
        String currency,
        String language,
        List<String> themeIds,
        String categoryId,
        boolean remindersEnabled
) {
}
