package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.common.geocoding.DomainGeocodingService;
import com.karma.platform.dto.EventDtos;
import com.karma.platform.model.EventStatus;
import com.karma.platform.model.RsvpStatus;
import com.karma.platform.persistence.entity.EventThemeEntity;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.OrganizerProfileEntity;
import com.karma.platform.persistence.entity.RsvpEntity;
import com.karma.platform.persistence.repository.CategoryRepository;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.EventThemeRepository;
import com.karma.platform.persistence.repository.OrganizerProfileRepository;
import com.karma.platform.persistence.repository.RsvpRepository;
import com.karma.platform.persistence.repository.ReviewRepository;
import com.karma.platform.persistence.repository.UserRepository;
import com.karma.platform.service.notification.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final RsvpRepository rsvpRepository;
    private final ReviewRepository reviewRepository;
    private final EventThemeRepository eventThemeRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final DomainGeocodingService domainGeocodingService;
    private final WaitlistService waitlistService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ApiMapper apiMapper;

    public EventService(
            EventRepository eventRepository,
            CategoryRepository categoryRepository,
            RsvpRepository rsvpRepository,
            ReviewRepository reviewRepository,
            EventThemeRepository eventThemeRepository,
            OrganizerProfileRepository organizerProfileRepository,
            DomainGeocodingService domainGeocodingService,
            WaitlistService waitlistService,
            UserRepository userRepository,
            EmailService emailService,
            ApiMapper apiMapper
    ) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.rsvpRepository = rsvpRepository;
        this.reviewRepository = reviewRepository;
        this.eventThemeRepository = eventThemeRepository;
        this.organizerProfileRepository = organizerProfileRepository;
        this.domainGeocodingService = domainGeocodingService;
        this.waitlistService = waitlistService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.apiMapper = apiMapper;
    }

    public List<EventDtos.EventResponse> list(String categorySlug, String q, String sort) {
        String categoryId = categorySlug == null ? null : categoryRepository.findBySlug(categorySlug).map(item -> item.getId()).orElse(null);
        Specification<EventEntity> specification = (root, query, criteriaBuilder) -> {
            java.util.ArrayList<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), EventStatus.PUBLISHED));
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), categoryId));
            }
            if (StringUtils.hasText(q)) {
                String pattern = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), pattern)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
        var events = eventRepository.findAll(specification, sortFor(sort)).stream();
        if ("popular".equalsIgnoreCase(sort)) {
            return events
                    .sorted(Comparator.comparingLong((EventEntity item) -> rsvpRepository.countByEventIdAndStatus(item.getId(), RsvpStatus.YES)).reversed())
                    .map(apiMapper::toEvent)
                    .toList();
        }
        return events.map(apiMapper::toEvent).toList();
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
        try {
            return eventRepository.findNearbyPublished(queryLat, queryLng, radius * 1000).stream()
                    .map(apiMapper::toEvent)
                    .toList();
        } catch (RuntimeException ignored) {
        }
        return eventRepository.findByStatus(EventStatus.PUBLISHED).stream()
                .filter(event -> distanceKm(queryLat, queryLng, event.getLatitude(), event.getLongitude()) <= radius || event.isOnline())
                .sorted(Comparator.comparingDouble(event -> distanceKm(queryLat, queryLng, event.getLatitude(), event.getLongitude())))
                .map(apiMapper::toEvent)
                .toList();
    }

    public List<EventDtos.EventResponse> managedEvents(String userId) {
        String organizerId = requireOrganizer(userId).getId();
        return eventRepository.findByOrganizerId(organizerId).stream()
                .sorted(Comparator.comparing(EventEntity::getStartDate))
                .map(apiMapper::toEvent)
                .toList();
    }

    @Transactional
    public EventDtos.EventResponse createEvent(String userId, EventDtos.UpsertEventRequest request) {
        OrganizerProfileEntity organizer = requireOrganizer(userId);
        EventEntity event = new EventEntity();
        event.setId(UUID.randomUUID().toString());
        event.setOrganizerId(organizer.getId());
        applyEventData(event, request);
        eventRepository.save(event);
        replaceThemes(event.getId(), request.themeIds());
        return apiMapper.toEvent(event);
    }

    @Transactional
    public EventDtos.EventResponse updateEvent(String userId, String eventId, EventDtos.UpsertEventRequest request) {
        EventEntity event = requireManagedEvent(userId, eventId);
        applyEventData(event, request);
        eventRepository.save(event);
        replaceThemes(event.getId(), request.themeIds());
        return apiMapper.toEvent(event);
    }

    @Transactional
    public void cancelEvent(String userId, String eventId) {
        EventEntity event = requireManagedEvent(userId, eventId);
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
        rsvpRepository.findByEventId(eventId).stream()
                .filter(rsvp -> rsvp.getStatus() == RsvpStatus.YES)
                .map(RsvpEntity::getUserId)
                .distinct()
                .forEach(attendeeId -> userRepository.findById(attendeeId).ifPresent(user ->
                        emailService.sendEventCancellationEmail(user, event)));
    }

    public EventDtos.EventDetailResponse detail(String slug) {
        EventEntity event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.event-not-found", "Event not found"));
        List<EventDtos.EventResponse> related = eventRepository.findByStatus(EventStatus.PUBLISHED).stream()
                .filter(item -> !item.getId().equals(event.getId()))
                .limit(3)
                .map(apiMapper::toEvent)
                .toList();
        List<EventDtos.ReviewResponse> reviews = reviewRepository.findByEventIdOrderByCreatedAtDesc(event.getId()).stream()
                .map(apiMapper::toReview)
                .toList();
        return new EventDtos.EventDetailResponse(apiMapper.toEvent(event), related, reviews);
    }

    public EventDtos.RsvpResponse rsvp(String eventId, String userId) {
        return rsvpRepository.findByEventIdAndUserId(eventId, userId).map(apiMapper::toRsvp).orElse(null);
    }

    @Transactional
    public EventDtos.RsvpResponse attend(String eventId, String userId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.event-not-found", "Event not found"));
        RsvpEntity rsvp = rsvpRepository.findByEventIdAndUserId(eventId, userId).orElseGet(() -> {
            RsvpEntity created = new RsvpEntity();
            created.setId(UUID.randomUUID().toString());
            created.setCreatedAt(LocalDateTime.now());
            return created;
        });
        int attendeeCount = Math.toIntExact(rsvpRepository.countByEventIdAndStatus(eventId, RsvpStatus.YES));
        if (rsvp.getStatus() == RsvpStatus.YES) {
            return apiMapper.toRsvp(rsvp);
        }
        RsvpStatus status = event.getMaxAttendees() != null && attendeeCount >= event.getMaxAttendees() ? RsvpStatus.WAITLISTED : RsvpStatus.YES;
        rsvp.setEventId(eventId);
        rsvp.setUserId(userId);
        rsvp.setStatus(status);
        rsvp.setWaitlistPosition(status == RsvpStatus.WAITLISTED ? waitlistService.nextPosition(eventId) : null);
        rsvp.setCheckedIn(false);
        rsvp.setNoShow(false);
        rsvp.setUpdatedAt(LocalDateTime.now());
        RsvpEntity saved = rsvpRepository.save(rsvp);
        if (status == RsvpStatus.YES) {
            userRepository.findById(userId).ifPresent(user -> emailService.sendRsvpConfirmationEmail(user, event));
        }
        return apiMapper.toRsvp(saved);
    }

    @Transactional
    public void cancelRsvp(String eventId, String userId) {
        rsvpRepository.findByEventIdAndUserId(eventId, userId).ifPresent(rsvp -> {
            boolean wasConfirmed = rsvp.getStatus() == RsvpStatus.YES;
            boolean wasWaitlisted = rsvp.getStatus() == RsvpStatus.WAITLISTED;
            rsvp.setStatus(RsvpStatus.NO);
            rsvp.setWaitlistPosition(null);
            rsvp.setCheckedIn(false);
            rsvp.setNoShow(false);
            rsvp.setUpdatedAt(LocalDateTime.now());
            rsvpRepository.save(rsvp);
            if (wasConfirmed) {
                RsvpEntity promoted = waitlistService.promoteFromWaitlist(eventId);
                if (promoted != null) {
                    eventRepository.findById(eventId).ifPresent(event ->
                            userRepository.findById(promoted.getUserId()).ifPresent(user ->
                                    emailService.sendWaitlistPromotionEmail(user, event)));
                }
            } else if (wasWaitlisted) {
                waitlistService.reorderWaitlist(eventId);
            }
        });
    }

    @Transactional
    public EventDtos.RsvpResponse checkIn(String userId, String eventId, String attendeeUserId) {
        requireManagedEvent(userId, eventId);
        RsvpEntity rsvp = requireAttendanceRecord(eventId, attendeeUserId);
        rsvp.setCheckedIn(true);
        rsvp.setNoShow(false);
        rsvp.setUpdatedAt(LocalDateTime.now());
        return apiMapper.toRsvp(rsvpRepository.save(rsvp));
    }

    @Transactional
    public EventDtos.RsvpResponse markNoShow(String userId, String eventId, String attendeeUserId) {
        requireManagedEvent(userId, eventId);
        RsvpEntity rsvp = requireAttendanceRecord(eventId, attendeeUserId);
        rsvp.setCheckedIn(false);
        rsvp.setNoShow(true);
        rsvp.setUpdatedAt(LocalDateTime.now());
        return apiMapper.toRsvp(rsvpRepository.save(rsvp));
    }

    private void applyEventData(EventEntity event, EventDtos.UpsertEventRequest request) {
        if (!StringUtils.hasText(request.title())
                || !StringUtils.hasText(request.city())
                || !StringUtils.hasText(request.country())
                || !StringUtils.hasText(request.categoryId())
                || !StringUtils.hasText(request.language())
                || request.startDate() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.validation", "Validation error");
        }
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.validation", "Validation error");
        }
        if (request.maxAttendees() != null && request.maxAttendees() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.validation", "Validation error");
        }
        if (!request.isFree() && (request.price() == null || request.price() < 0)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.validation", "Validation error");
        }
        event.setGroupId(request.groupId());
        event.setTitle(request.title().trim());
        event.setSlug(uniqueSlug(event.getId(), slugify(request.title())));
        event.setDescription(request.description());
        event.setCoverImageUrl(request.coverImageUrl());
        event.setStartDate(request.startDate());
        event.setEndDate(request.endDate());
        event.setVenueName(request.venueName());
        event.setAddress(request.address());
        event.setCity(request.city());
        event.setCountry(request.country());
        event.setOnline(request.isOnline());
        event.setHybrid(request.isHybrid());
        event.setOnlineUrl(request.onlineUrl());
        event.setStatus(parseStatus(request.status(), event.getStatus()));
        event.setFeatured(request.featured());
        event.setMaxAttendees(request.maxAttendees());
        event.setFree(request.isFree());
        event.setPrice(request.isFree() ? 0.0 : request.price());
        event.setCurrency(StringUtils.hasText(request.currency()) ? request.currency().trim().toUpperCase(Locale.ROOT) : "EUR");
        event.setLanguage(request.language().trim());
        event.setCategoryId(request.categoryId());
        event.setRemindersEnabled(request.remindersEnabled() == null || request.remindersEnabled());
        resolveEventCoordinates(event, request);
    }

    private void replaceThemes(String eventId, List<String> themeIds) {
        eventThemeRepository.deleteByEventId(eventId);
        if (themeIds == null) {
            return;
        }
        themeIds.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .forEach(themeId -> {
                    EventThemeEntity eventTheme = new EventThemeEntity();
                    eventTheme.setEventId(eventId);
                    eventTheme.setThemeId(themeId);
                    eventThemeRepository.save(eventTheme);
                });
    }

    private void resolveEventCoordinates(EventEntity event, EventDtos.UpsertEventRequest request) {
        if (request.latitude() != null && request.longitude() != null) {
            event.setLatitude(request.latitude());
            event.setLongitude(request.longitude());
            return;
        }
        try {
            var geocoded = domainGeocodingService.geocodeEventAddress(request.venueName(), request.address(), request.city(), request.country());
            if (geocoded.isPresent()) {
                event.setLatitude(geocoded.get().latitude());
                event.setLongitude(geocoded.get().longitude());
                return;
            }
        } catch (ApiException exception) {
            if (exception.getStatus() != HttpStatus.SERVICE_UNAVAILABLE) {
                throw exception;
            }
        }
        event.setLatitude(Objects.requireNonNullElse(request.latitude(), 0.0));
        event.setLongitude(Objects.requireNonNullElse(request.longitude(), 0.0));
    }

    private EventEntity requireManagedEvent(String userId, String eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.event-not-found", "Event not found"));
        OrganizerProfileEntity organizer = requireOrganizer(userId);
        if (!event.getOrganizerId().equals(organizer.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.organizer-access-denied", "Organizer access denied");
        }
        return event;
    }

    private OrganizerProfileEntity requireOrganizer(String userId) {
        return organizerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "error.organizer-profile-not-found", "Organizer profile not found"));
    }

    private RsvpEntity requireAttendanceRecord(String eventId, String userId) {
        return rsvpRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.membership-not-found", "Attendance not found"));
    }

    private Sort sortFor(String sort) {
        if ("popular".equalsIgnoreCase(sort)) {
            return Sort.unsorted();
        }
        if ("date_desc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "startDate");
        }
        return Sort.by(Sort.Direction.ASC, "startDate");
    }

    private EventStatus parseStatus(String rawStatus, EventStatus current) {
        if (!StringUtils.hasText(rawStatus)) {
            return current == null ? EventStatus.PUBLISHED : current;
        }
        return EventStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
    }

    private String uniqueSlug(String eventId, String base) {
        EventEntity existing = eventRepository.findBySlug(base).orElse(null);
        if (existing == null || existing.getId().equals(eventId)) {
            return base;
        }
        int suffix = 2;
        while (true) {
            String candidate = base + "-" + suffix;
            EventEntity item = eventRepository.findBySlug(candidate).orElse(null);
            if (item == null || item.getId().equals(eventId)) {
                return candidate;
            }
            suffix++;
        }
    }

    private String slugify(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
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
