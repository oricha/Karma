package com.karma.platform.service.notification;

import com.karma.platform.model.ReminderLogStatus;
import com.karma.platform.model.RsvpStatus;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.EventReviewRequestLogEntity;
import com.karma.platform.persistence.entity.RsvpEntity;
import com.karma.platform.persistence.entity.UserEntity;
import com.karma.platform.persistence.entity.UserPreferenceEntity;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.EventReviewRequestLogRepository;
import com.karma.platform.persistence.repository.ReviewRepository;
import com.karma.platform.persistence.repository.RsvpRepository;
import com.karma.platform.persistence.repository.UserPreferenceRepository;
import com.karma.platform.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PersistentReviewRequestService {

    private static final Logger log = LoggerFactory.getLogger(PersistentReviewRequestService.class);

    private final EventRepository eventRepository;
    private final RsvpRepository rsvpRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final ReviewRepository reviewRepository;
    private final EventReviewRequestLogRepository reviewRequestLogRepository;
    private final EmailService emailService;

    public PersistentReviewRequestService(
            EventRepository eventRepository,
            RsvpRepository rsvpRepository,
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository,
            ReviewRepository reviewRepository,
            EventReviewRequestLogRepository reviewRequestLogRepository,
            EmailService emailService
    ) {
        this.eventRepository = eventRepository;
        this.rsvpRepository = rsvpRepository;
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.reviewRepository = reviewRepository;
        this.reviewRequestLogRepository = reviewRequestLogRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void sendPendingReviewRequests() {
        LocalDateTime now = LocalDateTime.now();
        for (EventEntity event : eventRepository.findAll()) {
            LocalDateTime eventEnd = event.getEndDate() != null ? event.getEndDate() : event.getStartDate();
            if (eventEnd == null || !isInReviewWindow(eventEnd, now)) {
                continue;
            }
            for (RsvpEntity rsvp : rsvpRepository.findByEventId(event.getId())) {
                if (rsvp.getStatus() != RsvpStatus.YES || !rsvp.isCheckedIn()) {
                    continue;
                }
                if (reviewRepository.findByUserIdAndEventId(rsvp.getUserId(), event.getId()).isPresent()) {
                    continue;
                }
                if (reviewRequestLogRepository.findByEventIdAndUserId(event.getId(), rsvp.getUserId()).isPresent()) {
                    continue;
                }
                userRepository.findById(rsvp.getUserId()).ifPresent(user -> {
                    if (!reviewRemindersEnabled(user.getId())) {
                        return;
                    }
                    sendReviewRequest(user, event);
                });
            }
        }
    }

    private boolean reviewRemindersEnabled(String userId) {
        return userPreferenceRepository.findById(userId)
                .map(UserPreferenceEntity::isReviewReminders)
                .orElse(true);
    }

    private void sendReviewRequest(UserEntity user, EventEntity event) {
        EventReviewRequestLogEntity logEntry = new EventReviewRequestLogEntity();
        logEntry.setId(UUID.randomUUID().toString());
        logEntry.setEventId(event.getId());
        logEntry.setUserId(user.getId());
        logEntry.setLocale(user.getLocale());
        logEntry.setSentAt(LocalDateTime.now());
        try {
            emailService.sendReviewRequestEmail(user, event);
            logEntry.setStatus(ReminderLogStatus.SENT);
        } catch (RuntimeException exception) {
            log.warn("review_request_send_failed userId={} eventId={} reason={}",
                    user.getId(), event.getId(), exception.getMessage());
            logEntry.setStatus(ReminderLogStatus.FAILED);
        }
        reviewRequestLogRepository.save(logEntry);
    }

    private static boolean isInReviewWindow(LocalDateTime eventEnd, LocalDateTime now) {
        long hoursSinceEnd = Duration.between(eventEnd, now).toHours();
        return hoursSinceEnd >= 23 && hoursSinceEnd < 25;
    }
}
