package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.OrganizerDtos;
import com.karma.platform.model.EventStatus;
import com.karma.platform.model.OrderStatus;
import com.karma.platform.model.RsvpStatus;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.OrderEntity;
import com.karma.platform.persistence.entity.ReviewEntity;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.OrderRepository;
import com.karma.platform.persistence.repository.OrganizerProfileRepository;
import com.karma.platform.persistence.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import com.karma.platform.persistence.repository.RsvpRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class OrganizerService {

    private final OrganizerProfileRepository organizerProfileRepository;
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final RsvpRepository rsvpRepository;
    private final ReviewRepository reviewRepository;
    private final ApiMapper apiMapper;

    public OrganizerService(
            OrganizerProfileRepository organizerProfileRepository,
            EventRepository eventRepository,
            OrderRepository orderRepository,
            RsvpRepository rsvpRepository,
            ReviewRepository reviewRepository,
            ApiMapper apiMapper
    ) {
        this.organizerProfileRepository = organizerProfileRepository;
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
        this.rsvpRepository = rsvpRepository;
        this.reviewRepository = reviewRepository;
        this.apiMapper = apiMapper;
    }

    public OrganizerDtos.DashboardResponse dashboard(String userId) {
        String organizerId = organizerProfileRepository.findByUserId(userId)
                .map(item -> item.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "error.organizer-profile-not-found", "Organizer profile not found"));
        List<EventEntity> events = eventRepository.findByOrganizerId(organizerId);
        List<String> eventIds = events.stream().map(EventEntity::getId).toList();
        List<OrderEntity> orders = eventIds.isEmpty() ? List.of() : orderRepository.findByEventIdIn(eventIds);
        List<ReviewEntity> reviews = eventIds.isEmpty() ? List.of() : reviewRepository.findByEventIdIn(eventIds);
        int totalRsvps = events.stream().mapToInt(event -> (int) rsvpRepository.countByEventIdAndStatus(event.getId(), RsvpStatus.YES)).sum();
        int totalTickets = (int) orders.stream().filter(order -> order.getStatus() == OrderStatus.PAID).count();
        double totalRevenue = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PAID)
                .mapToDouble(OrderEntity::getTotalAmount)
                .sum();
        double averageRating = reviews.stream().mapToInt(ReviewEntity::getRating).average().orElse(0.0);
        List<OrganizerDtos.ActivityResponse> recentActivity = buildRecentActivity(events, orders, reviews);
        return new OrganizerDtos.DashboardResponse(
                (int) events.stream()
                        .filter(event -> event.getStatus() == EventStatus.PUBLISHED)
                        .filter(event -> event.getStartDate() != null && event.getStartDate().isAfter(LocalDateTime.now()))
                        .count(),
                totalRsvps,
                totalTickets,
                totalRevenue,
                Math.round(averageRating * 10.0) / 10.0,
                reviews.size(),
                events.stream()
                        .sorted(Comparator.comparing(EventEntity::getStartDate))
                        .map(apiMapper::toEvent)
                        .limit(5)
                        .toList(),
                recentActivity
        );
    }

    private List<OrganizerDtos.ActivityResponse> buildRecentActivity(List<EventEntity> events, List<OrderEntity> orders, List<ReviewEntity> reviews) {
        return Stream.concat(
                        orders.stream().map(order -> toOrderActivity(order, events)),
                        reviews.stream().map(review -> toReviewActivity(review, events))
                )
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(OrganizerDtos.ActivityResponse::occurredAt).reversed())
                .limit(8)
                .toList();
    }

    private OrganizerDtos.ActivityResponse toOrderActivity(OrderEntity order, List<EventEntity> events) {
        EventEntity event = events.stream().filter(item -> item.getId().equals(order.getEventId())).findFirst().orElse(null);
        if (event == null) {
            return null;
        }
        return new OrganizerDtos.ActivityResponse(
                "ORDER",
                "New ticket order",
                "Order " + order.getId() + " for " + formatAmount(order.getTotalAmount(), order.getCurrency()),
                order.getPurchasedAt().toString(),
                event.getId(),
                event.getSlug(),
                event.getTitle()
        );
    }

    private OrganizerDtos.ActivityResponse toReviewActivity(ReviewEntity review, List<EventEntity> events) {
        EventEntity event = events.stream().filter(item -> item.getId().equals(review.getEventId())).findFirst().orElse(null);
        if (event == null || review.getCreatedAt() == null) {
            return null;
        }
        return new OrganizerDtos.ActivityResponse(
                "REVIEW",
                "New review",
                review.getRating() + "/5 review received",
                review.getCreatedAt().toString(),
                event.getId(),
                event.getSlug(),
                event.getTitle()
        );
    }

    private String formatAmount(double amount, String currency) {
        return String.format("%.2f %s", amount, currency == null ? "EUR" : currency);
    }
}
