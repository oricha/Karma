package com.karma.platform.service.notification;

import com.karma.platform.model.ReminderType;
import com.karma.platform.persistence.entity.BlogPostEntity;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.OrderEntity;
import com.karma.platform.persistence.entity.UserEntity;

import java.util.List;

public interface EmailService {

    void sendWelcomeEmail(UserEntity user);

    void sendEmailVerificationEmail(UserEntity user, String verificationToken);

    void sendPasswordResetEmail(UserEntity user, String resetToken);

    void sendRsvpConfirmationEmail(UserEntity user, EventEntity event);

    void sendWaitlistPromotionEmail(UserEntity user, EventEntity event);

    void sendOrderConfirmationEmail(UserEntity user, OrderEntity order, EventEntity event);

    void sendEventCancellationEmail(UserEntity user, EventEntity event);

    void sendReviewRequestEmail(UserEntity user, EventEntity event);

    void sendNewEventNotificationEmail(UserEntity user, EventEntity event, String groupName);

    void sendWeeklyDigestEmail(UserEntity user, DigestContent digestContent);

    void sendPlatformNewsEmail(UserEntity user, List<BlogPostEntity> featuredPosts);

    void sendEventReminderEmail(UserEntity user, EventEntity event, ReminderType reminderType);

    record DigestItem(
            String eventId,
            String slug,
            String title,
            String description,
            String startsAt,
            String city,
            String venueName
    ) {
    }

    record DigestContent(
            List<DigestItem> groupEvents,
            List<DigestItem> recommendedEvents,
            List<DigestItem> popularEvents,
            String unsubscribeToken
    ) {
    }
}
