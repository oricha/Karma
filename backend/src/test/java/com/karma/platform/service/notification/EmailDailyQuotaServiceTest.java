package com.karma.platform.service.notification;

import com.karma.platform.config.NotificationProperties;
import com.karma.platform.persistence.entity.EmailDailySendCounterEntity;
import com.karma.platform.persistence.repository.EmailDailySendCounterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailDailyQuotaServiceTest {

    @Mock
    private EmailDailySendCounterRepository counterRepository;

    private EmailDailyQuotaService quotaService;

    @BeforeEach
    void setUp() {
        NotificationProperties properties = new NotificationProperties();
        properties.setDailyLimit(100);
        quotaService = new EmailDailyQuotaService(counterRepository, properties);
    }

    @Test
    void reservesQuotaForTransactionalSendsWhenNearDailyLimit() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        EmailDailySendCounterEntity counter = new EmailDailySendCounterEntity();
        counter.setSendDate(today);
        counter.setSentCount(70);
        when(counterRepository.findById(today)).thenReturn(Optional.of(counter));
        when(counterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertFalse(quotaService.tryConsume(EmailPriority.REMINDER));
        assertTrue(quotaService.tryConsume(EmailPriority.TRANSACTIONAL));

        ArgumentCaptor<EmailDailySendCounterEntity> saved = ArgumentCaptor.forClass(EmailDailySendCounterEntity.class);
        verify(counterRepository).save(saved.capture());
        assertTrue(saved.getValue().getSentCount() >= 71);
    }
}
