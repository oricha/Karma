package com.karma.platform.persistence.entity;

import com.karma.platform.model.NewsletterFrequency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_preferences")
public class UserPreferenceEntity extends AuditableEntity {

    @Id
    @Column(name = "user_id", length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "newsletter_frequency", nullable = false, length = 32)
    private NewsletterFrequency newsletterFrequency;

    @Column(name = "review_reminders", nullable = false)
    private boolean reviewReminders;

    @Column(name = "preferred_location", length = 255)
    private String preferredLocation;

    @Column
    private double latitude;

    @Column
    private double longitude;

    @Column(name = "location_radius_km", nullable = false)
    private int locationRadiusKm;

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

    public boolean isReviewReminders() {
        return reviewReminders;
    }

    public void setReviewReminders(boolean reviewReminders) {
        this.reviewReminders = reviewReminders;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getLocationRadiusKm() {
        return locationRadiusKm;
    }

    public void setLocationRadiusKm(int locationRadiusKm) {
        this.locationRadiusKm = locationRadiusKm;
    }
}
