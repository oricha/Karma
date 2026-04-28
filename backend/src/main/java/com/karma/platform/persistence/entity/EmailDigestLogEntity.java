package com.karma.platform.persistence.entity;

import com.karma.platform.model.NewsletterFrequency;
import com.karma.platform.model.ReminderLogStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_digest_logs")
public class EmailDigestLogEntity extends AuditableEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "newsletter_frequency", nullable = false, length = 32)
    private NewsletterFrequency newsletterFrequency;

    @Column(nullable = false, length = 12)
    private String locale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReminderLogStatus status;

    @Column(name = "last_digest_sent_at")
    private LocalDateTime lastDigestSentAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public NewsletterFrequency getNewsletterFrequency() {
        return newsletterFrequency;
    }

    public void setNewsletterFrequency(NewsletterFrequency newsletterFrequency) {
        this.newsletterFrequency = newsletterFrequency;
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

    public LocalDateTime getLastDigestSentAt() {
        return lastDigestSentAt;
    }

    public void setLastDigestSentAt(LocalDateTime lastDigestSentAt) {
        this.lastDigestSentAt = lastDigestSentAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
