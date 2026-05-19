package com.karma.platform.persistence.entity;

import com.karma.platform.model.ReminderLogStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_review_request_logs")
public class EventReviewRequestLogEntity extends AuditableEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 12)
    private String locale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReminderLogStatus status;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public ReminderLogStatus getStatus() {
        return status;
    }

    public void setStatus(ReminderLogStatus status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
