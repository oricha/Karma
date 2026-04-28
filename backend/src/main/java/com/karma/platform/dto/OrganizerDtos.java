package com.karma.platform.dto;

import java.util.List;

public final class OrganizerDtos {

    private OrganizerDtos() {
    }

    public record DashboardResponse(
            int upcomingEvents,
            int totalRsvps,
            int totalTicketsSold,
            double totalRevenue,
            double averageRating,
            int totalReviews,
            List<EventDtos.EventResponse> recentEvents,
            List<ActivityResponse> recentActivity
    ) {
    }

    public record ActivityResponse(
            String type,
            String title,
            String description,
            String occurredAt,
            String eventId,
            String eventSlug,
            String eventTitle
    ) {
    }
}
