package com.karma.platform.service.notification;

import com.karma.platform.model.ReminderType;
import com.karma.platform.persistence.entity.BlogPostEntity;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.OrderEntity;
import com.karma.platform.persistence.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NoopEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(NoopEmailService.class);

    @Override
    public void sendWelcomeEmail(UserEntity user) {
        log.info("email_disabled skipped=welcome userId={}", user.getId());
    }

    @Override
    public void sendEmailVerificationEmail(UserEntity user, String verificationToken) {
        log.info("email_disabled skipped=email_verification userId={}", user.getId());
    }

    @Override
    public void sendPasswordResetEmail(UserEntity user, String resetToken) {
        log.info("email_disabled skipped=password_reset userId={}", user.getId());
    }

    @Override
    public void sendRsvpConfirmationEmail(UserEntity user, EventEntity event) {
        log.info("email_disabled skipped=rsvp_confirmation userId={} eventId={}", user.getId(), event.getId());
    }

    @Override
    public void sendWaitlistPromotionEmail(UserEntity user, EventEntity event) {
        log.info("email_disabled skipped=waitlist_promotion userId={} eventId={}", user.getId(), event.getId());
    }

    @Override
    public void sendOrderConfirmationEmail(UserEntity user, OrderEntity order, EventEntity event) {
        log.info("email_disabled skipped=order_confirmation userId={} orderId={}", user.getId(), order.getId());
    }

    @Override
    public void sendEventCancellationEmail(UserEntity user, EventEntity event) {
        log.info("email_disabled skipped=event_cancellation userId={} eventId={}", user.getId(), event.getId());
    }

    @Override
    public void sendReviewRequestEmail(UserEntity user, EventEntity event) {
        log.info("email_disabled skipped=review_request userId={} eventId={}", user.getId(), event.getId());
    }

    @Override
    public void sendNewEventNotificationEmail(UserEntity user, EventEntity event, String groupName) {
        log.info("email_disabled skipped=new_group_event userId={} eventId={}", user.getId(), event.getId());
    }

    @Override
    public void sendWeeklyDigestEmail(UserEntity user, DigestContent digestContent) {
        log.info("email_disabled skipped=weekly_digest userId={}", user.getId());
    }

    @Override
    public void sendPlatformNewsEmail(UserEntity user, List<BlogPostEntity> featuredPosts) {
        log.info("email_disabled skipped=platform_news userId={} count={}", user.getId(), featuredPosts.size());
    }

    @Override
    public void sendEventReminderEmail(UserEntity user, EventEntity event, ReminderType reminderType) {
        log.info("email_disabled skipped=event_reminder userId={} eventId={} reminderType={}", user.getId(), event.getId(), reminderType);
    }
}
