package com.karma.platform.service.notification;

import com.karma.platform.persistence.entity.EmailDeferredSendEntity;
import com.karma.platform.persistence.repository.EmailDeferredSendRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class EmailDeferredQueueService {

    private final EmailDeferredSendRepository deferredSendRepository;

    public EmailDeferredQueueService(EmailDeferredSendRepository deferredSendRepository) {
        this.deferredSendRepository = deferredSendRepository;
    }

    @Transactional
    public void enqueue(String recipientEmail, String subject, String htmlBody, EmailPriority priority) {
        EmailDeferredSendEntity row = new EmailDeferredSendEntity();
        row.setId(UUID.randomUUID().toString());
        row.setRecipientEmail(recipientEmail);
        row.setSubject(subject);
        row.setHtmlBody(htmlBody);
        row.setPriority(priority.name());
        row.setScheduledFor(nextUtcMidnight());
        row.setCreatedAt(Instant.now());
        deferredSendRepository.save(row);
    }

    @Transactional(readOnly = true)
    public List<EmailDeferredSendEntity> dueSends() {
        return deferredSendRepository
                .findBySentAtIsNullAndScheduledForLessThanEqualOrderByScheduledForAsc(Instant.now())
                .stream()
                .sorted(Comparator.comparing((EmailDeferredSendEntity row) ->
                        EmailPriority.valueOf(row.getPriority()).rank()))
                .toList();
    }

    @Transactional
    public void markSent(EmailDeferredSendEntity row) {
        row.setSentAt(Instant.now());
        deferredSendRepository.save(row);
    }

    private static Instant nextUtcMidnight() {
        return LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
