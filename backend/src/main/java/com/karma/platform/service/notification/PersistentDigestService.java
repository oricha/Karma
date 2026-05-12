package com.karma.platform.service.notification;

import com.karma.platform.model.EventStatus;
import com.karma.platform.model.NewsletterFrequency;
import com.karma.platform.model.ReminderLogStatus;
import com.karma.platform.model.RsvpStatus;
import com.karma.platform.persistence.entity.BlogPostEntity;
import com.karma.platform.persistence.entity.EmailDigestLogEntity;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.GroupMembershipEntity;
import com.karma.platform.persistence.entity.UserEntity;
import com.karma.platform.persistence.entity.UserPreferenceEntity;
import com.karma.platform.persistence.entity.UserThemePreferenceEntity;
import com.karma.platform.persistence.repository.BlogPostRepository;
import com.karma.platform.persistence.repository.EmailDigestLogRepository;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.EventThemeRepository;
import com.karma.platform.persistence.repository.GroupMembershipRepository;
import com.karma.platform.persistence.repository.RsvpRepository;
import com.karma.platform.persistence.repository.UserPreferenceRepository;
import com.karma.platform.persistence.repository.UserRepository;
import com.karma.platform.persistence.repository.UserThemePreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PersistentDigestService implements DigestService {

    private static final Logger log = LoggerFactory.getLogger(PersistentDigestService.class);

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final EventRepository eventRepository;
    private final EventThemeRepository eventThemeRepository;
    private final UserThemePreferenceRepository userThemePreferenceRepository;
    private final EmailDigestLogRepository emailDigestLogRepository;
    private final BlogPostRepository blogPostRepository;
    private final RsvpRepository rsvpRepository;
    private final EmailService emailService;

    public PersistentDigestService(
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository,
            GroupMembershipRepository groupMembershipRepository,
            EventRepository eventRepository,
            EventThemeRepository eventThemeRepository,
            UserThemePreferenceRepository userThemePreferenceRepository,
            EmailDigestLogRepository emailDigestLogRepository,
            BlogPostRepository blogPostRepository,
            RsvpRepository rsvpRepository,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.eventRepository = eventRepository;
        this.eventThemeRepository = eventThemeRepository;
        this.userThemePreferenceRepository = userThemePreferenceRepository;
        this.emailDigestLogRepository = emailDigestLogRepository;
        this.blogPostRepository = blogPostRepository;
        this.rsvpRepository = rsvpRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void sendEligibleDigests() {
        LocalDateTime now = LocalDateTime.now();
        for (UserPreferenceEntity preference : userPreferenceRepository.findAll()) {
            if (!shouldSend(preference, now)) {
                continue;
            }
            userRepository.findById(preference.getUserId()).ifPresent(user -> sendDigest(user, preference, now));
        }
    }

    private void sendDigest(UserEntity user, UserPreferenceEntity preference, LocalDateTime now) {
        try {
            if (preference.getNewsletterFrequency() == NewsletterFrequency.KARMA_ONLY) {
                List<BlogPostEntity> featuredPosts = blogPostRepository.findTop3ByPublishedTrueAndFeaturedTrueOrderByPublishedAtDesc();
                emailService.sendPlatformNewsEmail(user, featuredPosts);
            } else {
                emailService.sendWeeklyDigestEmail(user, buildDigest(user, preference));
            }
            EmailDigestLogEntity logEntry = new EmailDigestLogEntity();
            logEntry.setId(UUID.randomUUID().toString());
            logEntry.setUserId(user.getId());
            logEntry.setNewsletterFrequency(preference.getNewsletterFrequency());
            logEntry.setLocale(user.getLocale());
            logEntry.setStatus(ReminderLogStatus.SENT);
            logEntry.setSentAt(now);
            logEntry.setLastDigestSentAt(now);
            emailDigestLogRepository.save(logEntry);
        } catch (RuntimeException exception) {
            log.warn("digest_send_failed userId={} reason={}", user.getId(), exception.getMessage());
            EmailDigestLogEntity logEntry = new EmailDigestLogEntity();
            logEntry.setId(UUID.randomUUID().toString());
            logEntry.setUserId(user.getId());
            logEntry.setNewsletterFrequency(preference.getNewsletterFrequency());
            logEntry.setLocale(user.getLocale());
            logEntry.setStatus(ReminderLogStatus.FAILED);
            logEntry.setSentAt(now);
            logEntry.setLastDigestSentAt(now);
            emailDigestLogRepository.save(logEntry);
        }
    }

    private boolean shouldSend(UserPreferenceEntity preference, LocalDateTime now) {
        if (preference.getNewsletterFrequency() == null || preference.getNewsletterFrequency() == NewsletterFrequency.NEVER) {
            return false;
        }
        return emailDigestLogRepository.findTopByUserIdOrderBySentAtDesc(preference.getUserId())
                .map(logEntry -> switch (preference.getNewsletterFrequency()) {
                    case WEEKLY, KARMA_ONLY -> ChronoUnit.DAYS.between(logEntry.getSentAt().toLocalDate(), now.toLocalDate()) >= 7;
                    case BIWEEKLY -> ChronoUnit.DAYS.between(logEntry.getSentAt().toLocalDate(), now.toLocalDate()) >= 14;
                    case MONTHLY -> ChronoUnit.MONTHS.between(logEntry.getSentAt().toLocalDate().withDayOfMonth(1), now.toLocalDate().withDayOfMonth(1)) >= 1;
                    case NEVER -> false;
                })
                .orElse(true);
    }

    private EmailService.DigestContent buildDigest(UserEntity user, UserPreferenceEntity preference) {
        Set<String> seen = new HashSet<>();
        List<EmailService.DigestItem> groupEvents = groupMembershipRepository.findByUserIdAndStatus(user.getId(), "ACTIVE").stream()
                .map(GroupMembershipEntity::getGroupId)
                .distinct()
                .flatMap(groupId -> eventRepository.findByGroupId(groupId).stream())
                .filter(event -> includeUpcoming(event, 14))
                .sorted(java.util.Comparator.comparing(EventEntity::getStartDate))
                .map(this::toDigestItem)
                .filter(item -> seen.add(item.eventId()))
                .limit(5)
                .toList();

        Set<String> themeIds = userThemePreferenceRepository.findByUserId(user.getId()).stream()
                .map(UserThemePreferenceEntity::getThemeId)
                .collect(java.util.stream.Collectors.toSet());
        List<EmailService.DigestItem> recommendedEvents = eventRepository.findByStatus(EventStatus.PUBLISHED).stream()
                .filter(event -> includeUpcoming(event, 14))
                .filter(event -> distanceKm(preference.getLatitude(), preference.getLongitude(), event.getLatitude(), event.getLongitude()) <= preference.getLocationRadiusKm())
                .filter(event -> eventThemeRepository.findByEventId(event.getId()).stream().anyMatch(theme -> themeIds.contains(theme.getThemeId())))
                .sorted(java.util.Comparator.comparing(EventEntity::getStartDate))
                .map(this::toDigestItem)
                .filter(item -> seen.add(item.eventId()))
                .limit(5)
                .toList();

        List<EmailService.DigestItem> popularEvents = eventRepository.findByStatus(EventStatus.PUBLISHED).stream()
                .filter(event -> includeUpcoming(event, 7))
                .filter(event -> distanceKm(preference.getLatitude(), preference.getLongitude(), event.getLatitude(), event.getLongitude()) <= preference.getLocationRadiusKm())
                .sorted(java.util.Comparator.comparingLong((EventEntity event) -> rsvpRepository.countByEventIdAndStatus(event.getId(), RsvpStatus.YES)).reversed())
                .map(this::toDigestItem)
                .filter(item -> seen.add(item.eventId()))
                .limit(3)
                .toList();

        return new EmailService.DigestContent(groupEvents, recommendedEvents, popularEvents, user.getId());
    }

    private boolean includeUpcoming(EventEntity event, int daysForward) {
        return event.getStatus() == EventStatus.PUBLISHED
                && event.getStartDate() != null
                && event.getStartDate().isAfter(LocalDateTime.now())
                && !event.getStartDate().isAfter(LocalDateTime.now().plusDays(daysForward));
    }

    private EmailService.DigestItem toDigestItem(EventEntity event) {
        return new EmailService.DigestItem(
                event.getId(),
                event.getSlug(),
                event.getTitle(),
                event.getDescription(),
                event.getStartDate() == null ? null : event.getStartDate().toString(),
                event.getCity(),
                event.getVenueName()
        );
    }

    private double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        if (lat1 == 0 && lng1 == 0) {
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
