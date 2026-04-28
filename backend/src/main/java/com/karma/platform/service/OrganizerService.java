package com.karma.platform.service;

import com.karma.platform.dto.OrganizerDtos;
import com.karma.platform.model.EventStatus;
import com.karma.platform.model.RsvpStatus;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.OrderRepository;
import com.karma.platform.persistence.repository.OrganizerProfileRepository;
import com.karma.platform.persistence.repository.RsvpRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizerService {

    private final OrganizerProfileRepository organizerProfileRepository;
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final RsvpRepository rsvpRepository;
    private final ApiMapper apiMapper;

    public OrganizerService(
            OrganizerProfileRepository organizerProfileRepository,
            EventRepository eventRepository,
            OrderRepository orderRepository,
            RsvpRepository rsvpRepository,
            ApiMapper apiMapper
    ) {
        this.organizerProfileRepository = organizerProfileRepository;
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
        this.rsvpRepository = rsvpRepository;
        this.apiMapper = apiMapper;
    }

    public OrganizerDtos.DashboardResponse dashboard(String userId) {
        String organizerId = organizerProfileRepository.findByUserId(userId).map(item -> item.getId()).orElse(null);
        List<EventEntity> events = eventRepository.findByOrganizerId(organizerId);
        List<String> eventIds = events.stream().map(EventEntity::getId).toList();
        var orders = eventIds.isEmpty() ? List.<com.karma.platform.persistence.entity.OrderEntity>of() : orderRepository.findByEventIdIn(eventIds);
        int totalRsvps = events.stream().mapToInt(event -> (int) rsvpRepository.countByEventIdAndStatus(event.getId(), RsvpStatus.YES)).sum();
        int totalTickets = orders.size();
        double totalRevenue = orders.stream().mapToDouble(order -> order.getTotalAmount()).sum();
        return new OrganizerDtos.DashboardResponse(
                (int) events.stream().filter(event -> event.getStatus() == EventStatus.PUBLISHED).count(),
                totalRsvps,
                totalTickets,
                totalRevenue,
                events.stream().map(apiMapper::toEvent).limit(5).toList()
        );
    }
}
