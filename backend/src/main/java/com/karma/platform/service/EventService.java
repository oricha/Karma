package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.EventDtos;
import com.karma.platform.model.Event;
import com.karma.platform.model.EventStatus;
import com.karma.platform.model.Rsvp;
import com.karma.platform.model.RsvpStatus;
import com.karma.platform.seed.PlatformDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class EventService {

    private final PlatformDataStore dataStore;
    private final ApiMapper apiMapper;

    public EventService(PlatformDataStore dataStore, ApiMapper apiMapper) {
        this.dataStore = dataStore;
        this.apiMapper = apiMapper;
    }

    public List<EventDtos.EventResponse> list(String categorySlug, String q) {
        return dataStore.events().stream()
                .filter(event -> event.status() == EventStatus.PUBLISHED)
                .filter(event -> categorySlug == null || matchesCategory(event, categorySlug))
                .filter(event -> q == null || event.title().toLowerCase().contains(q.toLowerCase()) || (event.description() != null && event.description().toLowerCase().contains(q.toLowerCase())))
                .sorted(Comparator.comparing(Event::startDate))
                .map(apiMapper::toEvent)
                .toList();
    }

    public List<EventDtos.EventResponse> popular() {
        return dataStore.events().stream()
                .filter(event -> event.status() == EventStatus.PUBLISHED)
                .sorted(Comparator.comparingInt((Event item) -> dataStore.attendeeCount(item.id())).reversed())
                .map(apiMapper::toEvent)
                .toList();
    }

    public List<EventDtos.EventResponse> nearby(Double lat, Double lng, Integer radiusKm) {
        double queryLat = lat == null ? 40.4168 : lat;
        double queryLng = lng == null ? -3.7038 : lng;
        int radius = radiusKm == null ? 50 : radiusKm;
        return dataStore.events().stream()
                .filter(event -> event.status() == EventStatus.PUBLISHED)
                .filter(event -> distanceKm(queryLat, queryLng, event.latitude(), event.longitude()) <= radius || event.isOnline())
                .sorted(Comparator.comparingDouble(event -> distanceKm(queryLat, queryLng, event.latitude(), event.longitude())))
                .map(apiMapper::toEvent)
                .toList();
    }

    public EventDtos.EventDetailResponse detail(String slug) {
        Event event = dataStore.eventBySlug(slug).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
        List<EventDtos.EventResponse> related = dataStore.events().stream()
                .filter(item -> !item.id().equals(event.id()) && item.status() == EventStatus.PUBLISHED)
                .limit(3)
                .map(apiMapper::toEvent)
                .toList();
        return new EventDtos.EventDetailResponse(apiMapper.toEvent(event), related);
    }

    public EventDtos.RsvpResponse rsvp(String eventId, String userId) {
        return dataStore.userRsvp(eventId, userId).map(apiMapper::toRsvp).orElse(null);
    }

    public EventDtos.RsvpResponse attend(String eventId, String userId) {
        Event event = dataStore.eventById(eventId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
        int attendeeCount = dataStore.attendeeCount(eventId);
        RsvpStatus status = event.maxAttendees() != null && attendeeCount >= event.maxAttendees() ? RsvpStatus.WAITLISTED : RsvpStatus.YES;
        Integer waitlistPosition = status == RsvpStatus.WAITLISTED
                ? (int) dataStore.rsvpsForEvent(eventId).stream().filter(item -> item.status() == RsvpStatus.WAITLISTED).count() + 1
                : null;
        Rsvp existing = dataStore.userRsvp(eventId, userId).orElse(null);
        Rsvp updated = new Rsvp(existing == null ? dataStore.id() : existing.id(), eventId, userId, status, waitlistPosition, false, false);
        dataStore.saveRsvp(updated);
        return apiMapper.toRsvp(updated);
    }

    public void cancelRsvp(String eventId, String userId) {
        dataStore.userRsvp(eventId, userId).ifPresent(rsvp -> dataStore.saveRsvp(new Rsvp(rsvp.id(), eventId, userId, RsvpStatus.NO, null, false, false)));
    }

    private boolean matchesCategory(Event event, String categorySlug) {
        return dataStore.categories().stream()
                .anyMatch(category -> category.id().equals(event.categoryId()) && category.slug().equals(categorySlug));
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
