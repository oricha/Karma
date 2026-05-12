package com.karma.platform.controller;

import com.karma.platform.common.CurrentUser;
import com.karma.platform.dto.EventDtos;
import com.karma.platform.service.EventService;
import com.karma.platform.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final ReviewService reviewService;
    private final CurrentUser currentUser;

    public EventController(EventService eventService, ReviewService reviewService, CurrentUser currentUser) {
        this.eventService = eventService;
        this.reviewService = reviewService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<EventDtos.EventResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort
    ) {
        return eventService.list(category, q, sort);
    }

    @GetMapping("/search")
    public List<EventDtos.EventResponse> search(@RequestParam(required = false) String q, @RequestParam(required = false) String sort) {
        return eventService.list(null, q, sort);
    }

    @GetMapping("/nearby")
    public List<EventDtos.EventResponse> nearby(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false, name = "radiusKm") Integer radiusKm
    ) {
        return eventService.nearby(lat, lng, radiusKm == null ? radius : radiusKm);
    }

    @GetMapping("/popular")
    public List<EventDtos.EventResponse> popular() {
        return eventService.popular();
    }

    @GetMapping("/category/{slug}")
    public List<EventDtos.EventResponse> byCategory(@PathVariable String slug) {
        return eventService.list(slug, null, null);
    }

    @GetMapping("/{slug}")
    public EventDtos.EventDetailResponse detail(@PathVariable String slug) {
        return eventService.detail(slug);
    }

    @PostMapping("/{id}/rsvp")
    public EventDtos.RsvpResponse attend(@PathVariable String id) {
        return eventService.attend(id, currentUser.id());
    }

    @DeleteMapping("/{id}/rsvp")
    public void cancelRsvp(@PathVariable String id) {
        eventService.cancelRsvp(id, currentUser.id());
    }

    @GetMapping("/{id}/rsvp")
    public EventDtos.RsvpResponse rsvp(@PathVariable String id) {
        return eventService.rsvp(id, currentUser.id());
    }

    @GetMapping("/{id}/reviews")
    public List<EventDtos.ReviewResponse> reviews(@PathVariable String id) {
        return reviewService.list(id);
    }

    @PostMapping("/{id}/reviews")
    public EventDtos.ReviewResponse createReview(@PathVariable String id, @RequestBody @jakarta.validation.Valid EventDtos.UpsertReviewRequest request) {
        return reviewService.create(id, currentUser.id(), request);
    }

    @PutMapping("/{id}/reviews")
    public EventDtos.ReviewResponse updateReview(@PathVariable String id, @RequestBody @jakarta.validation.Valid EventDtos.UpsertReviewRequest request) {
        return reviewService.update(id, currentUser.id(), request);
    }

    @DeleteMapping("/{id}/reviews")
    public void deleteReview(@PathVariable String id) {
        reviewService.delete(id, currentUser.id());
    }
}
