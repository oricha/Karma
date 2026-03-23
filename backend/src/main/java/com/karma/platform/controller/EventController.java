package com.karma.platform.controller;

import com.karma.platform.common.CurrentUser;
import com.karma.platform.dto.EventDtos;
import com.karma.platform.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final CurrentUser currentUser;

    public EventController(EventService eventService, CurrentUser currentUser) {
        this.eventService = eventService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<EventDtos.EventResponse> list(@RequestParam(required = false) String category, @RequestParam(required = false) String q) {
        return eventService.list(category, q);
    }

    @GetMapping("/search")
    public List<EventDtos.EventResponse> search(@RequestParam(required = false) String q) {
        return eventService.list(null, q);
    }

    @GetMapping("/nearby")
    public List<EventDtos.EventResponse> nearby(@RequestParam(required = false) Double lat, @RequestParam(required = false) Double lng, @RequestParam(required = false) Integer radius) {
        return eventService.nearby(lat, lng, radius);
    }

    @GetMapping("/popular")
    public List<EventDtos.EventResponse> popular() {
        return eventService.popular();
    }

    @GetMapping("/category/{slug}")
    public List<EventDtos.EventResponse> byCategory(@PathVariable String slug) {
        return eventService.list(slug, null);
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
}
