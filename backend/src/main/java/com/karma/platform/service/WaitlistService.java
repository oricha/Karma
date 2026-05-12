package com.karma.platform.service;

import com.karma.platform.model.RsvpStatus;
import com.karma.platform.persistence.entity.RsvpEntity;
import com.karma.platform.persistence.repository.RsvpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class WaitlistService {

    private final RsvpRepository rsvpRepository;

    public WaitlistService(RsvpRepository rsvpRepository) {
        this.rsvpRepository = rsvpRepository;
    }

    @Transactional
    public void reorderWaitlist(String eventId) {
        List<RsvpEntity> waitlisted = waitlisted(eventId);
        int position = 1;
        for (RsvpEntity item : waitlisted) {
            item.setWaitlistPosition(position++);
            item.setUpdatedAt(LocalDateTime.now());
        }
        rsvpRepository.saveAll(waitlisted);
    }

    @Transactional
    public RsvpEntity promoteFromWaitlist(String eventId) {
        List<RsvpEntity> waitlisted = waitlisted(eventId);
        if (waitlisted.isEmpty()) {
            return null;
        }
        RsvpEntity promoted = waitlisted.getFirst();
        promoted.setStatus(RsvpStatus.YES);
        promoted.setWaitlistPosition(null);
        promoted.setUpdatedAt(LocalDateTime.now());
        promoted.setCheckedIn(false);
        promoted.setNoShow(false);
        rsvpRepository.save(promoted);
        reorderWaitlist(eventId);
        return promoted;
    }

    @Transactional
    public void removeFromWaitlist(String eventId, String userId) {
        rsvpRepository.findByEventIdAndUserId(eventId, userId)
                .filter(rsvp -> rsvp.getStatus() == RsvpStatus.WAITLISTED)
                .ifPresent(rsvp -> {
                    rsvp.setStatus(RsvpStatus.NO);
                    rsvp.setWaitlistPosition(null);
                    rsvp.setUpdatedAt(LocalDateTime.now());
                    rsvpRepository.save(rsvp);
                    reorderWaitlist(eventId);
                });
    }

    public int nextPosition(String eventId) {
        return waitlisted(eventId).size() + 1;
    }

    private List<RsvpEntity> waitlisted(String eventId) {
        return rsvpRepository.findByEventId(eventId).stream()
                .filter(item -> item.getStatus() == RsvpStatus.WAITLISTED)
                .sorted(Comparator.comparing(item -> item.getWaitlistPosition() == null ? Integer.MAX_VALUE : item.getWaitlistPosition()))
                .toList();
    }
}
