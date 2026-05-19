package com.karma.platform.service.notification;

import com.karma.platform.model.ReminderLogStatus;
import com.karma.platform.model.RsvpStatus;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.RsvpEntity;
import com.karma.platform.persistence.entity.UserEntity;
import com.karma.platform.persistence.entity.UserPreferenceEntity;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.EventReviewRequestLogRepository;
import com.karma.platform.persistence.repository.ReviewRepository;
import com.karma.platform.persistence.repository.RsvpRepository;
import com.karma.platform.persistence.repository.UserPreferenceRepository;
import com.karma.platform.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistentReviewRequestServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private RsvpRepository rsvpRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserPreferenceRepository userPreferenceRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private EventReviewRequestLogRepository reviewRequestLogRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private PersistentReviewRequestService reviewRequestService;

    @Test
    void sendsReviewRequestForCheckedInAttendeeAfterEventEnds() {
        EventEntity event = new EventEntity();
        event.setId("event-1");
        event.setEndDate(LocalDateTime.now().minusHours(24));

        RsvpEntity rsvp = new RsvpEntity();
        rsvp.setEventId("event-1");
        rsvp.setUserId("user-1");
        rsvp.setStatus(RsvpStatus.YES);
        rsvp.setCheckedIn(true);

        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setLocale("es");

        UserPreferenceEntity preference = new UserPreferenceEntity();
        preference.setUserId("user-1");
        preference.setReviewReminders(true);

        when(eventRepository.findAll()).thenReturn(List.of(event));
        when(rsvpRepository.findByEventId("event-1")).thenReturn(List.of(rsvp));
        when(reviewRepository.findByUserIdAndEventId("user-1", "event-1")).thenReturn(Optional.empty());
        when(reviewRequestLogRepository.findByEventIdAndUserId("event-1", "user-1")).thenReturn(Optional.empty());
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userPreferenceRepository.findById("user-1")).thenReturn(Optional.of(preference));
        when(reviewRequestLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        reviewRequestService.sendPendingReviewRequests();

        verify(emailService).sendReviewRequestEmail(user, event);
        ArgumentCaptor<com.karma.platform.persistence.entity.EventReviewRequestLogEntity> saved =
                ArgumentCaptor.forClass(com.karma.platform.persistence.entity.EventReviewRequestLogEntity.class);
        verify(reviewRequestLogRepository).save(saved.capture());
        assertEquals(ReminderLogStatus.SENT, saved.getValue().getStatus());
    }

    @Test
    void skipsWhenReviewRemindersDisabled() {
        EventEntity event = new EventEntity();
        event.setId("event-1");
        event.setEndDate(LocalDateTime.now().minusHours(24));

        RsvpEntity rsvp = new RsvpEntity();
        rsvp.setEventId("event-1");
        rsvp.setUserId("user-1");
        rsvp.setStatus(RsvpStatus.YES);
        rsvp.setCheckedIn(true);

        UserEntity user = new UserEntity();
        user.setId("user-1");

        UserPreferenceEntity preference = new UserPreferenceEntity();
        preference.setReviewReminders(false);

        when(eventRepository.findAll()).thenReturn(List.of(event));
        when(rsvpRepository.findByEventId("event-1")).thenReturn(List.of(rsvp));
        when(reviewRepository.findByUserIdAndEventId("user-1", "event-1")).thenReturn(Optional.empty());
        when(reviewRequestLogRepository.findByEventIdAndUserId("event-1", "user-1")).thenReturn(Optional.empty());
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userPreferenceRepository.findById("user-1")).thenReturn(Optional.of(preference));

        reviewRequestService.sendPendingReviewRequests();

        verify(emailService, never()).sendReviewRequestEmail(any(), any());
        verify(reviewRequestLogRepository, never()).save(any());
    }
}
