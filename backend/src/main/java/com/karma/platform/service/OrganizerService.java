package com.karma.platform.service;

import com.karma.platform.dto.OrganizerDtos;
import com.karma.platform.model.EventStatus;
import com.karma.platform.seed.PlatformDataStore;
import org.springframework.stereotype.Service;

@Service
public class OrganizerService {

    private final PlatformDataStore dataStore;
    private final ApiMapper apiMapper;

    public OrganizerService(PlatformDataStore dataStore, ApiMapper apiMapper) {
        this.dataStore = dataStore;
        this.apiMapper = apiMapper;
    }

    public OrganizerDtos.DashboardResponse dashboard(String userId) {
        String organizerId = dataStore.organizerByUserId(userId).map(item -> item.id()).orElse(null);
        var events = dataStore.events().stream()
                .filter(event -> event.organizerId().equals(organizerId))
                .toList();
        int totalRsvps = events.stream().mapToInt(event -> dataStore.attendeeCount(event.id())).sum();
        int totalTickets = (int) dataStore.ordersByUser(userId).stream().count();
        double totalRevenue = dataStore.ordersByUser(userId).stream().mapToDouble(order -> order.totalAmount()).sum();
        return new OrganizerDtos.DashboardResponse(
                (int) events.stream().filter(event -> event.status() == EventStatus.PUBLISHED).count(),
                totalRsvps,
                totalTickets,
                totalRevenue,
                events.stream().map(apiMapper::toEvent).limit(5).toList()
        );
    }
}
