package com.karma.platform.service.notification;

import com.karma.platform.config.NotificationProperties;
import com.karma.platform.persistence.entity.EmailDailySendCounterEntity;
import com.karma.platform.persistence.repository.EmailDailySendCounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class EmailDailyQuotaService {

    private static final Logger log = LoggerFactory.getLogger(EmailDailyQuotaService.class);

    private final EmailDailySendCounterRepository counterRepository;
    private final NotificationProperties notificationProperties;

    public EmailDailyQuotaService(
            EmailDailySendCounterRepository counterRepository,
            NotificationProperties notificationProperties
    ) {
        this.counterRepository = counterRepository;
        this.notificationProperties = notificationProperties;
    }

    @Transactional
    public boolean tryConsume(EmailPriority priority) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        EmailDailySendCounterEntity counter = counterRepository.findById(today)
                .orElseGet(() -> {
                    EmailDailySendCounterEntity created = new EmailDailySendCounterEntity();
                    created.setSendDate(today);
                    created.setSentCount(0);
                    created.setUpdatedAt(Instant.now());
                    return created;
                });

        int limit = notificationProperties.getDailyLimit();
        int sent = counter.getSentCount();
        if (sent >= limit) {
            return false;
        }

        int reservedForTransactional = (limit * EmailPriority.TRANSACTIONAL.reservedPercentOfQuota()) / 100;
        int nonTransactionalCap = limit - reservedForTransactional;
        if (priority != EmailPriority.TRANSACTIONAL && sent >= nonTransactionalCap) {
            return false;
        }

        counter.setSentCount(sent + 1);
        counter.setUpdatedAt(Instant.now());
        counterRepository.save(counter);
        if (counter.getSentCount() == limit || counter.getSentCount() % 10 == 0) {
            log.info("Email usage: {}/{} ({}%)", counter.getSentCount(), limit,
                    Math.round((counter.getSentCount() * 100.0) / limit));
        }
        return true;
    }

    public int remainingQuota() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int sent = counterRepository.findById(today).map(EmailDailySendCounterEntity::getSentCount).orElse(0);
        return Math.max(0, notificationProperties.getDailyLimit() - sent);
    }
}
