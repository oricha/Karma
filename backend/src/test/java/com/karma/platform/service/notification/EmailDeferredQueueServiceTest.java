package com.karma.platform.service.notification;

import com.karma.platform.persistence.entity.EmailDeferredSendEntity;
import com.karma.platform.persistence.repository.EmailDeferredSendRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailDeferredQueueServiceTest {

    @Mock
    private EmailDeferredSendRepository deferredSendRepository;

    @InjectMocks
    private EmailDeferredQueueService deferredQueueService;

    @Test
    void enqueuesDeferredEmailForNextUtcMidnight() {
        when(deferredSendRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        deferredQueueService.enqueue("user@example.com", "Subject", "<p>Body</p>", EmailPriority.DIGEST);

        ArgumentCaptor<EmailDeferredSendEntity> captor = ArgumentCaptor.forClass(EmailDeferredSendEntity.class);
        verify(deferredSendRepository).save(captor.capture());
        EmailDeferredSendEntity saved = captor.getValue();
        assertEquals("user@example.com", saved.getRecipientEmail());
        assertEquals("DIGEST", saved.getPriority());
        org.junit.jupiter.api.Assertions.assertNotNull(saved.getScheduledFor());
    }

    @Test
    void sortsDueSendsByPriorityRank() {
        EmailDeferredSendEntity news = deferredRow("NEWS");
        EmailDeferredSendEntity transactional = deferredRow("TRANSACTIONAL");
        when(deferredSendRepository.findBySentAtIsNullAndScheduledForLessThanEqualOrderByScheduledForAsc(any()))
                .thenReturn(List.of(news, transactional));

        List<EmailDeferredSendEntity> due = deferredQueueService.dueSends();

        assertEquals("TRANSACTIONAL", due.getFirst().getPriority());
    }

    private static EmailDeferredSendEntity deferredRow(String priority) {
        EmailDeferredSendEntity row = new EmailDeferredSendEntity();
        row.setPriority(priority);
        row.setScheduledFor(Instant.now().minusSeconds(60));
        return row;
    }
}
