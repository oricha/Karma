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
            List<EventDtos.EventResponse> recentEvents
    ) {
    }
}
