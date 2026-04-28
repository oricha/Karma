package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.EventDtos;
import com.karma.platform.model.EventStatus;
import com.karma.platform.model.RsvpStatus;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.RsvpEntity;
import com.karma.platform.persistence.repository.CategoryRepository;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.RsvpRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final RsvpRepository rsvpRepository;
    private final ApiMapper apiMapper;

    public EventService(EventRepository eventRepository, CategoryRepository categoryRepository, RsvpRepository rsvpRepository, ApiMapper apiMapper) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.rsvpRepository = rsvpRepository;
        this.apiMapper = apiMapper;
    }

    public List<EventDtos.EventResponse> list(String categorySlug, String q) {
        String categoryId = categorySlug == null ? null : categoryRepository.findBySlug(categorySlug).map(item -> item.getId()).orElse(null);
        return eventRepository.findByStatus(EventStatus.PUBLISHED).stream()
                .filter(event -> categoryId == null || categoryId.equals(event.getCategoryId()))
                .filter(event -> q == null || containsIgnoreCase(event.getTitle(), q) || containsIgnoreCase(event.getDescription(), q))
                .sorted(Comparator.comparing(EventEntity::getStartDate))
                .map(apiMapper::toEvent)
                .toList();
    }

    public List<EventDtos.EventResponse> popular() {
        return eventRepository.findByStatus(EventStatus.PUBLISHED).stream()
                .sorted(Comparator.comparingLong((EventEntity item) -> rsvpRepository.countByEventIdAndStatus(item.getId(), RsvpStatus.YES)).reversed())
                .map(apiMapper::toEvent)
                .toList();
    }

    public List<EventDtos.EventResponse> nearby(Double lat, Double lng, Integer radiusKm) {
        double queryLat = lat == null ? 40.4168 : lat;
        double queryLng = lng == null ? -3.7038 : lng;
        int radius = radiusKm == null ? 50 : radiusKm;
        return eventRepository.findByStatus(EventStatus.PUBLISHED).stream()
                .filter(event -> distanceKm(queryLat, queryLng, event.getLatitude(), event.getLongitude()) <= radius || event.isOnline())
                .sorted(Comparator.comparingDouble(event -> distanceKm(queryLat, queryLng, event.getLatitude(), event.getLongitude())))
                .map(apiMapper::toEvent)
                .toList();
    }

    public EventDtos.EventDetailResponse detail(String slug) {
        EventEntity event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.event-not-found", "Event not found"));
        List<EventDtos.EventResponse> related = eventRepository.findByStatus(EventStatus.PUBLISHED).stream()
                .filter(item -> !item.getId().equals(event.getId()))
                .limit(3)
                .map(apiMapper::toEvent)
                .toList();
        return new EventDtos.EventDetailResponse(apiMapper.toEvent(event), related);
    }

    public EventDtos.RsvpResponse rsvp(String eventId, String userId) {
        return rsvpRepository.findByEventIdAndUserId(eventId, userId).map(apiMapper::toRsvp).orElse(null);
    }

    @Transactional
    public EventDtos.RsvpResponse attend(String eventId, String userId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.event-not-found", "Event not found"));
        int attendeeCount = Math.toIntExact(rsvpRepository.countByEventIdAndStatus(eventId, RsvpStatus.YES));
        RsvpStatus status = event.getMaxAttendees() != null && attendeeCount >= event.getMaxAttendees() ? RsvpStatus.WAITLISTED : RsvpStatus.YES;
        Integer waitlistPosition = status == RsvpStatus.WAITLISTED
                ? (int) rsvpRepository.findByEventId(eventId).stream().filter(item -> item.getStatus() == RsvpStatus.WAITLISTED).count() + 1
                : null;

        RsvpEntity rsvp = rsvpRepository.findByEventIdAndUserId(eventId, userId).orElseGet(() -> {
            RsvpEntity created = new RsvpEntity();
            created.setId(UUID.randomUUID().toString());
            created.setCreatedAt(LocalDateTime.now());
            return created;
        });
        rsvp.setEventId(eventId);
        rsvp.setUserId(userId);
        rsvp.setStatus(status);
        rsvp.setWaitlistPosition(waitlistPosition);
        rsvp.setCheckedIn(false);
        rsvp.setNoShow(false);
        rsvp.setUpdatedAt(LocalDateTime.now());
        return apiMapper.toRsvp(rsvpRepository.save(rsvp));
    }

    @Transactional
    public void cancelRsvp(String eventId, String userId) {
        rsvpRepository.findByEventIdAndUserId(eventId, userId).ifPresent(rsvp -> {
            rsvp.setStatus(RsvpStatus.NO);
            rsvp.setWaitlistPosition(null);
            rsvp.setUpdatedAt(LocalDateTime.now());
            rsvpRepository.save(rsvp);
        });
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query.toLowerCase());
    }

    private double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        if (lat2 == 0 && lng2 == 0) {
            return 0;
        }
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * earthRadiusKm * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
