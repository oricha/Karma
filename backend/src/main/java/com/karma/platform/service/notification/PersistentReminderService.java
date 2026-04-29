package com.karma.platform.service.notification;

import com.karma.platform.model.OrderStatus;
import com.karma.platform.model.ReminderLogStatus;
import com.karma.platform.model.ReminderType;
import com.karma.platform.model.RsvpStatus;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.EventReminderLogEntity;
import com.karma.platform.persistence.entity.OrderEntity;
import com.karma.platform.persistence.entity.RsvpEntity;
import com.karma.platform.persistence.entity.UserEntity;
import com.karma.platform.persistence.repository.EventReminderLogRepository;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.OrderRepository;
import com.karma.platform.persistence.repository.RsvpRepository;
import com.karma.platform.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class PersistentReminderService {

    private static final Logger log = LoggerFactory.getLogger(PersistentReminderService.class);

    private final EventRepository eventRepository;
    private final RsvpRepository rsvpRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EventReminderLogRepository eventReminderLogRepository;
    private final EmailService emailService;

    public PersistentReminderService(
            EventRepository eventRepository,
            RsvpRepository rsvpRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            EventReminderLogRepository eventReminderLogRepository,
            EmailService emailService
    ) {
        this.eventRepository = eventRepository;
        this.rsvpRepository = rsvpRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.eventReminderLogRepository = eventReminderLogRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void sendReminders(ReminderType reminderType) {
        for (EventEntity event : eventRepository.findAll()) {
            if (!event.isRemindersEnabled() || event.getStartDate() == null || !matchesWindow(event.getStartDate(), reminderType)) {
                continue;
            }
            Set<String> userIds = new HashSet<>();
            rsvpRepository.findByEventId(event.getId()).stream()
                    .filter(rsvp -> rsvp.getStatus() == RsvpStatus.YES)
                    .map(RsvpEntity::getUserId)
                    .forEach(userIds::add);
            orderRepository.findByEventIdIn(java.util.List.of(event.getId())).stream()
                    .filter(order -> order.getStatus() == OrderStatus.PAID)
                    .map(OrderEntity::getUserId)
                    .forEach(userIds::add);
            for (String userId : userIds) {
                if (eventReminderLogRepository.findByEventIdAndUserIdAndReminderType(event.getId(), userId, reminderType).isPresent()) {
                    continue;
                }
                userRepository.findById(userId).ifPresent(user -> sendReminder(user, event, reminderType));
            }
        }
    }

    private void sendReminder(UserEntity user, EventEntity event, ReminderType reminderType) {
        EventReminderLogEntity logEntry = new EventReminderLogEntity();
        logEntry.setId(UUID.randomUUID().toString());
        logEntry.setEventId(event.getId());
        logEntry.setUserId(user.getId());
        logEntry.setReminderType(reminderType);
        logEntry.setLocale(user.getLocale());
        logEntry.setSentAt(LocalDateTime.now());
        try {
            emailService.sendEventReminderEmail(user, event, reminderType);
            logEntry.setStatus(ReminderLogStatus.SENT);
        } catch (RuntimeException exception) {
            log.warn("reminder_send_failed userId={} eventId={} type={} reason={}", user.getId(), event.getId(), reminderType, exception.getMessage());
            logEntry.setStatus(ReminderLogStatus.FAILED);
        }
        eventReminderLogRepository.save(logEntry);
    }

    private boolean matchesWindow(LocalDateTime startDate, ReminderType reminderType) {
        Duration duration = Duration.between(LocalDateTime.now(), startDate);
        long hours = duration.toHours();
        return switch (reminderType) {
            case SEVEN_DAYS -> hours >= 168 && hours < 192;
            case ONE_DAY -> hours >= 24 && hours < 48;
            case TWO_HOURS -> hours >= 2 && hours < 3;
        };
    }
}
