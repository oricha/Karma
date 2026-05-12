package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.OrderDtos;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.OrganizerProfileEntity;
import com.karma.platform.persistence.entity.TicketTypeEntity;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.OrganizerProfileRepository;
import com.karma.platform.persistence.repository.TicketTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final ApiMapper apiMapper;

    public TicketTypeService(
            TicketTypeRepository ticketTypeRepository,
            EventRepository eventRepository,
            OrganizerProfileRepository organizerProfileRepository,
            ApiMapper apiMapper
    ) {
        this.ticketTypeRepository = ticketTypeRepository;
        this.eventRepository = eventRepository;
        this.organizerProfileRepository = organizerProfileRepository;
        this.apiMapper = apiMapper;
    }

    public List<OrderDtos.TicketTypeResponse> listForOrganizerEvent(String userId, String eventId) {
        requireManagedEvent(userId, eventId);
        return ticketTypeRepository.findByEventId(eventId).stream()
                .sorted(Comparator.comparing(TicketTypeEntity::getName))
                .map(apiMapper::toTicketType)
                .toList();
    }

    @Transactional
    public OrderDtos.TicketTypeResponse create(String userId, String eventId, OrderDtos.UpsertTicketTypeRequest request) {
        requireManagedEvent(userId, eventId);
        TicketTypeEntity ticketType = new TicketTypeEntity();
        ticketType.setId(UUID.randomUUID().toString());
        ticketType.setEventId(eventId);
        apply(ticketType, request);
        return apiMapper.toTicketType(ticketTypeRepository.save(ticketType));
    }

    @Transactional
    public OrderDtos.TicketTypeResponse update(String userId, String eventId, String ticketTypeId, OrderDtos.UpsertTicketTypeRequest request) {
        requireManagedEvent(userId, eventId);
        TicketTypeEntity ticketType = ticketTypeRepository.findByEventIdAndId(eventId, ticketTypeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.ticket-type-not-found", "Ticket type not found"));
        apply(ticketType, request);
        return apiMapper.toTicketType(ticketTypeRepository.save(ticketType));
    }

    private void apply(TicketTypeEntity ticketType, OrderDtos.UpsertTicketTypeRequest request) {
        if (!StringUtils.hasText(request.name()) || !StringUtils.hasText(request.currency()) || request.price() < 0 || request.quantity() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.validation", "Validation error");
        }
        if (request.saleStart() != null && request.saleEnd() != null && request.saleEnd().isBefore(request.saleStart())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.validation", "Validation error");
        }
        ticketType.setName(request.name().trim());
        ticketType.setDescription(request.description());
        ticketType.setPrice(request.price());
        ticketType.setCurrency(request.currency().trim().toUpperCase());
        ticketType.setQuantity(request.quantity());
        ticketType.setEarlyBirdPrice(request.earlyBirdPrice());
        ticketType.setEarlyBirdQuantity(request.earlyBirdQuantity());
        ticketType.setEarlyBirdDeadline(request.earlyBirdDeadline());
        ticketType.setSaleStart(request.saleStart());
        ticketType.setSaleEnd(request.saleEnd());
    }

    private EventEntity requireManagedEvent(String userId, String eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.event-not-found", "Event not found"));
        OrganizerProfileEntity organizer = organizerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "error.organizer-profile-not-found", "Organizer profile not found"));
        if (!event.getOrganizerId().equals(organizer.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.organizer-access-denied", "Organizer access denied");
        }
        return event;
    }
}
