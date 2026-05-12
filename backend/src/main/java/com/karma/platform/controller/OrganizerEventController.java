package com.karma.platform.controller;

import com.karma.platform.common.CurrentUser;
import com.karma.platform.dto.EventDtos;
import com.karma.platform.dto.OrderDtos;
import com.karma.platform.service.EventService;
import com.karma.platform.service.TicketTypeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organizers/me/events")
public class OrganizerEventController {

    private final EventService eventService;
    private final TicketTypeService ticketTypeService;
    private final CurrentUser currentUser;

    public OrganizerEventController(EventService eventService, TicketTypeService ticketTypeService, CurrentUser currentUser) {
        this.eventService = eventService;
        this.ticketTypeService = ticketTypeService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<EventDtos.EventResponse> list() {
        return eventService.managedEvents(currentUser.id());
    }

    @PostMapping
    public EventDtos.EventResponse create(@RequestBody @jakarta.validation.Valid EventDtos.UpsertEventRequest request) {
        return eventService.createEvent(currentUser.id(), request);
    }

    @PutMapping("/{id}")
    public EventDtos.EventResponse update(@PathVariable String id, @RequestBody @jakarta.validation.Valid EventDtos.UpsertEventRequest request) {
        return eventService.updateEvent(currentUser.id(), id, request);
    }

    @DeleteMapping("/{id}")
    public void cancel(@PathVariable String id) {
        eventService.cancelEvent(currentUser.id(), id);
    }

    @PostMapping("/{id}/checkin")
    public EventDtos.RsvpResponse checkIn(@PathVariable String id, @RequestBody @jakarta.validation.Valid EventDtos.AttendanceUpdateRequest request) {
        return eventService.checkIn(currentUser.id(), id, request.userId());
    }

    @PostMapping("/{id}/no-show")
    public EventDtos.RsvpResponse markNoShow(@PathVariable String id, @RequestBody @jakarta.validation.Valid EventDtos.AttendanceUpdateRequest request) {
        return eventService.markNoShow(currentUser.id(), id, request.userId());
    }

    @GetMapping("/{id}/tickets")
    public List<OrderDtos.TicketTypeResponse> ticketTypes(@PathVariable String id) {
        return ticketTypeService.listForOrganizerEvent(currentUser.id(), id);
    }

    @PostMapping("/{id}/tickets")
    public OrderDtos.TicketTypeResponse createTicketType(@PathVariable String id, @RequestBody OrderDtos.UpsertTicketTypeRequest request) {
        return ticketTypeService.create(currentUser.id(), id, request);
    }

    @PutMapping("/{id}/tickets/{ticketTypeId}")
    public OrderDtos.TicketTypeResponse updateTicketType(
            @PathVariable String id,
            @PathVariable String ticketTypeId,
            @RequestBody OrderDtos.UpsertTicketTypeRequest request
    ) {
        return ticketTypeService.update(currentUser.id(), id, ticketTypeId, request);
    }
}
