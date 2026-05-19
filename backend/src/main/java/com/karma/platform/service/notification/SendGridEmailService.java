package com.karma.platform.service.notification;

import com.karma.platform.config.NotificationEmailProperties;
import com.karma.platform.config.NotificationProperties;
import com.karma.platform.model.ReminderType;
import com.karma.platform.persistence.entity.BlogPostEntity;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.OrderEntity;
import com.karma.platform.persistence.entity.UserEntity;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SendGridEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailService.class);

    private final SendGrid sendGrid;
    private final EmailTemplateRenderer templateRenderer;
    private final NotificationEmailProperties emailProperties;
    private final NotificationProperties notificationProperties;
    private final EmailDailyQuotaService dailyQuotaService;
    private final EmailDeferredQueueService deferredQueueService;

    public SendGridEmailService(
            SendGrid sendGrid,
            EmailTemplateRenderer templateRenderer,
            NotificationEmailProperties emailProperties,
            NotificationProperties notificationProperties,
            EmailDailyQuotaService dailyQuotaService,
            EmailDeferredQueueService deferredQueueService
    ) {
        this.sendGrid = sendGrid;
        this.templateRenderer = templateRenderer;
        this.emailProperties = emailProperties;
        this.notificationProperties = notificationProperties;
        this.dailyQuotaService = dailyQuotaService;
        this.deferredQueueService = deferredQueueService;
    }

    @Override
    public void sendWelcomeEmail(UserEntity user) {
        send(user, subject("email.welcome.subject", user), EmailTemplate.WELCOME, Map.of("user", user), EmailPriority.TRANSACTIONAL);
    }

    @Override
    public void sendEmailVerificationEmail(UserEntity user, String verificationToken) {
        send(user, subject("email.verification.subject", user), EmailTemplate.EMAIL_VERIFICATION, Map.of(
                "user", user,
                "verificationToken", verificationToken,
                "verificationLink", notificationProperties.getUnsubscribeBaseUrl() + "/verify-email?token=" + verificationToken
        ), EmailPriority.TRANSACTIONAL);
    }

    @Override
    public void sendPasswordResetEmail(UserEntity user, String resetToken) {
        send(user, subject("email.password-reset.subject", user), EmailTemplate.PASSWORD_RESET, Map.of(
                "user", user,
                "resetToken", resetToken,
                "resetLink", notificationProperties.getUnsubscribeBaseUrl() + "/reset-password?token=" + resetToken
        ), EmailPriority.TRANSACTIONAL);
    }

    @Override
    public void sendRsvpConfirmationEmail(UserEntity user, EventEntity event) {
        send(user, subject("email.rsvp-confirmation.subject", user), EmailTemplate.RSVP_CONFIRMATION,
                Map.of("user", user, "event", event), EmailPriority.TRANSACTIONAL);
    }

    @Override
    public void sendWaitlistPromotionEmail(UserEntity user, EventEntity event) {
        send(user, subject("email.waitlist-promotion.subject", user), EmailTemplate.WAITLIST_PROMOTION,
                Map.of("user", user, "event", event), EmailPriority.TRANSACTIONAL);
    }

    @Override
    public void sendOrderConfirmationEmail(UserEntity user, OrderEntity order, EventEntity event) {
        send(user, subject("email.order-confirmation.subject", user), EmailTemplate.ORDER_CONFIRMATION,
                Map.of("user", user, "order", order, "event", event), EmailPriority.TRANSACTIONAL);
    }

    @Override
    public void sendEventCancellationEmail(UserEntity user, EventEntity event) {
        send(user, subject("email.event-cancellation.subject", user), EmailTemplate.EVENT_CANCELLATION,
                Map.of("user", user, "event", event), EmailPriority.TRANSACTIONAL);
    }

    @Override
    public void sendReviewRequestEmail(UserEntity user, EventEntity event) {
        send(user, subject("email.review-request.subject", user), EmailTemplate.REVIEW_REQUEST,
                Map.of("user", user, "event", event), EmailPriority.REMINDER);
    }

    @Override
    public void sendNewEventNotificationEmail(UserEntity user, EventEntity event, String groupName) {
        send(user, subject("email.new-group-event.subject", user), EmailTemplate.NEW_GROUP_EVENT,
                Map.of("user", user, "event", event, "groupName", groupName), EmailPriority.NEWS);
    }

    @Override
    public void sendWeeklyDigestEmail(UserEntity user, DigestContent digestContent) {
        send(user, subject("email.weekly-digest.subject", user), EmailTemplate.WEEKLY_DIGEST, Map.of(
                "user", user,
                "digest", digestContent,
                "unsubscribeToken", digestContent.unsubscribeToken()
        ), EmailPriority.DIGEST);
    }

    @Override
    public void sendPlatformNewsEmail(UserEntity user, List<BlogPostEntity> featuredPosts) {
        send(user, subject("email.platform-news.subject", user), EmailTemplate.PLATFORM_NEWS, Map.of(
                "user", user,
                "featuredPosts", featuredPosts,
                "unsubscribeToken", user.getId()
        ), EmailPriority.NEWS);
    }

    @Override
    public void sendEventReminderEmail(UserEntity user, EventEntity event, ReminderType reminderType) {
        EmailTemplate template = switch (reminderType) {
            case SEVEN_DAYS -> EmailTemplate.EVENT_REMINDER_7DAY;
            case ONE_DAY -> EmailTemplate.EVENT_REMINDER_1DAY;
            case TWO_HOURS -> EmailTemplate.EVENT_REMINDER_2HOUR;
        };
        String key = switch (reminderType) {
            case SEVEN_DAYS -> "email.event-reminder-7day.subject";
            case ONE_DAY -> "email.event-reminder-1day.subject";
            case TWO_HOURS -> "email.event-reminder-2hour.subject";
        };
        send(user, subject(key, user), template, Map.of("user", user, "event", event, "reminderType", reminderType),
                EmailPriority.REMINDER);
    }

    private void send(
            UserEntity user,
            String subject,
            EmailTemplate template,
            Map<String, Object> variables,
            EmailPriority priority
    ) {
        EmailRetryTemplate.executeWithRetry(() -> {
            String body = templateRenderer.render(template, userLocale(user), variables);
            if (!dailyQuotaService.tryConsume(priority)) {
                deferredQueueService.enqueue(user.getEmail(), subject, body, priority);
                log.info("Deferred email {} to {} (priority {})", template.name(), user.getEmail(), priority);
                return null;
            }
            sendMail(subject, user.getEmail(), body);
            return null;
        }, "send_email:" + template.name());
    }

    private void sendMail(String subject, String recipientEmail, String body) {
        Mail mail = new Mail(
                new Email(emailProperties.getFromEmail(), emailProperties.getFromName()),
                subject,
                new Email(recipientEmail),
                new Content("text/html", body)
        );
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        try {
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            if (response.getStatusCode() >= 400) {
                if (response.getStatusCode() < 500) {
                    throw new NonRetryableEmailException("SendGrid rejected request with status " + response.getStatusCode());
                }
                throw new IllegalStateException("SendGrid rejected request with status " + response.getStatusCode());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("SendGrid request failed", exception);
        }
    }

    private String subject(String key, UserEntity user) {
        return templateRenderer.subject(key, userLocale(user));
    }

    private static EmailTemplateRenderer.UserLocale userLocale(UserEntity user) {
        return EmailTemplateRenderer.userLocale(user.getLocale());
    }
}
