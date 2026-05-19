package com.karma.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "email_daily_send_counter")
public class EmailDailySendCounterEntity {

    @Id
    @Column(name = "send_date")
    private LocalDate sendDate;

    @Column(name = "sent_count", nullable = false)
    private int sentCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LocalDate getSendDate() {
        return sendDate;
    }

    public void setSendDate(LocalDate sendDate) {
        this.sendDate = sendDate;
    }

    public int getSentCount() {
        return sentCount;
    }

    public void setSentCount(int sentCount) {
        this.sentCount = sentCount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
