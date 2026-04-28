package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.EventDtos;
import com.karma.platform.model.OrderStatus;
import com.karma.platform.model.RsvpStatus;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.ReviewEntity;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.OrderRepository;
import com.karma.platform.persistence.repository.ReviewRepository;
import com.karma.platform.persistence.repository.RsvpRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final EventRepository eventRepository;
    private final ReviewRepository reviewRepository;
    private final RsvpRepository rsvpRepository;
    private final OrderRepository orderRepository;
    private final ApiMapper apiMapper;

    public ReviewService(
            EventRepository eventRepository,
            ReviewRepository reviewRepository,
            RsvpRepository rsvpRepository,
            OrderRepository orderRepository,
            ApiMapper apiMapper
    ) {
        this.eventRepository = eventRepository;
        this.reviewRepository = reviewRepository;
        this.rsvpRepository = rsvpRepository;
        this.orderRepository = orderRepository;
        this.apiMapper = apiMapper;
    }

    public List<EventDtos.ReviewResponse> list(String eventId) {
        requireEvent(eventId);
        return reviewRepository.findByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(apiMapper::toReview)
                .toList();
    }

    @Transactional
    public EventDtos.ReviewResponse create(String eventId, String userId, EventDtos.UpsertReviewRequest request) {
        requireEvent(eventId);
        ensureCanReview(eventId, userId);
        if (reviewRepository.findByUserIdAndEventId(userId, eventId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "error.review-already-exists", "Review already exists");
        }
        ReviewEntity review = new ReviewEntity();
        review.setId(UUID.randomUUID().toString());
        review.setEventId(eventId);
        review.setUserId(userId);
        review.setRating(request.rating());
        review.setComment(normalizeComment(request.comment()));
        return apiMapper.toReview(reviewRepository.save(review));
    }

    @Transactional
    public EventDtos.ReviewResponse update(String eventId, String userId, EventDtos.UpsertReviewRequest request) {
        requireEvent(eventId);
        ReviewEntity review = reviewRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.review-not-found", "Review not found"));
        review.setRating(request.rating());
        review.setComment(normalizeComment(request.comment()));
        return apiMapper.toReview(reviewRepository.save(review));
    }

    @Transactional
    public void delete(String eventId, String userId) {
        requireEvent(eventId);
        ReviewEntity review = reviewRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.review-not-found", "Review not found"));
        reviewRepository.delete(review);
    }

    private EventEntity requireEvent(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.event-not-found", "Event not found"));
    }

    private void ensureCanReview(String eventId, String userId) {
        boolean attendedWithRsvp = rsvpRepository.existsByEventIdAndUserIdAndStatus(eventId, userId, RsvpStatus.YES);
        boolean attendedWithOrder = orderRepository.findByUserIdOrderByPurchasedAtDesc(userId).stream()
                .anyMatch(order -> order.getEventId().equals(eventId) && order.getStatus() == OrderStatus.PAID);
        if (!attendedWithRsvp && !attendedWithOrder) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.review-attendance-required", "Attending the event is required before leaving a review");
        }
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        String normalized = comment.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
