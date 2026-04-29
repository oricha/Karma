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
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SendGridEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailService.class);

    private final SendGrid sendGrid;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;
    private final NotificationEmailProperties emailProperties;
    private final NotificationProperties notificationProperties;
    private final Map<String, Integer> sentPerHour = new ConcurrentHashMap<>();

    public SendGridEmailService(
            SendGrid sendGrid,
            TemplateEngine templateEngine,
            MessageSource messageSource,
            NotificationEmailProperties emailProperties,
            NotificationProperties notificationProperties
    ) {
        this.sendGrid = sendGrid;
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
        this.emailProperties = emailProperties;
        this.notificationProperties = notificationProperties;
    }

    @Override
    public void sendWelcomeEmail(UserEntity user) {
        send(user, subject("email.welcome.subject", user), EmailTemplate.WELCOME, Map.of("user", user));
    }

    @Override
    public void sendEmailVerificationEmail(UserEntity user, String verificationToken) {
        send(user, subject("email.verification.subject", user), EmailTemplate.EMAIL_VERIFICATION, Map.of(
                "user", user,
                "verificationToken", verificationToken,
                "verificationLink", notificationProperties.getUnsubscribeBaseUrl() + "/verify-email?token=" + verificationToken
        ));
    }

    @Override
    public void sendPasswordResetEmail(UserEntity user, String resetToken) {
        send(user, subject("email.password-reset.subject", user), EmailTemplate.PASSWORD_RESET, Map.of(
                "user", user,
                "resetToken", resetToken,
                "resetLink", notificationProperties.getUnsubscribeBaseUrl() + "/reset-password?token=" + resetToken
        ));
    }

    @Override
    public void sendRsvpConfirmationEmail(UserEntity user, EventEntity event) {
        send(user, subject("email.rsvp-confirmation.subject", user), EmailTemplate.RSVP_CONFIRMATION, Map.of("user", user, "event", event));
    }

    @Override
    public void sendWaitlistPromotionEmail(UserEntity user, EventEntity event) {
        send(user, subject("email.waitlist-promotion.subject", user), EmailTemplate.WAITLIST_PROMOTION, Map.of("user", user, "event", event));
    }

    @Override
    public void sendOrderConfirmationEmail(UserEntity user, OrderEntity order, EventEntity event) {
        send(user, subject("email.order-confirmation.subject", user), EmailTemplate.ORDER_CONFIRMATION, Map.of("user", user, "order", order, "event", event));
    }

    @Override
    public void sendEventCancellationEmail(UserEntity user, EventEntity event) {
        send(user, subject("email.event-cancellation.subject", user), EmailTemplate.EVENT_CANCELLATION, Map.of("user", user, "event", event));
    }

    @Override
    public void sendReviewRequestEmail(UserEntity user, EventEntity event) {
        send(user, subject("email.review-request.subject", user), EmailTemplate.REVIEW_REQUEST, Map.of("user", user, "event", event));
    }

    @Override
    public void sendNewEventNotificationEmail(UserEntity user, EventEntity event, String groupName) {
        send(user, subject("email.new-group-event.subject", user), EmailTemplate.NEW_GROUP_EVENT, Map.of("user", user, "event", event, "groupName", groupName));
    }

    @Override
    public void sendWeeklyDigestEmail(UserEntity user, DigestContent digestContent) {
        send(user, subject("email.weekly-digest.subject", user), EmailTemplate.WEEKLY_DIGEST, Map.of(
                "user", user,
                "digest", digestContent,
                "unsubscribeToken", digestContent.unsubscribeToken()
        ));
    }

    @Override
    public void sendPlatformNewsEmail(UserEntity user, List<BlogPostEntity> featuredPosts) {
        send(user, subject("email.platform-news.subject", user), EmailTemplate.PLATFORM_NEWS, Map.of(
                "user", user,
                "featuredPosts", featuredPosts,
                "unsubscribeToken", user.getId()
        ));
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
        send(user, subject(key, user), template, Map.of("user", user, "event", event, "reminderType", reminderType));
    }

    private void send(UserEntity user, String subject, EmailTemplate template, Map<String, Object> variables) {
        EmailRetryTemplate.executeWithRetry(() -> {
            checkRateLimit();
            String body = render(template, locale(user), variables);
            sendMail(subject, user.getEmail(), body);
            recordSend();
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
                throw new IllegalStateException("SendGrid rejected request with status " + response.getStatusCode());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("SendGrid request failed", exception);
        }
    }

    private String subject(String key, UserEntity user) {
        return messageSource.getMessage(key, null, key, locale(user));
    }

    private Locale locale(UserEntity user) {
        return "en".equalsIgnoreCase(user.getLocale()) ? Locale.ENGLISH : new Locale("es");
    }

    private String render(EmailTemplate template, Locale locale, Map<String, Object> variables) {
        Context context = new Context(locale);
        context.setVariables(variables);
        String name = Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) ? template.getTemplatePathEn() : template.getTemplatePathEs();
        return templateEngine.process(name, context);
    }

    private void checkRateLimit() {
        String hourKey = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0).toString();
        int sent = sentPerHour.getOrDefault(hourKey, 0);
        if (sent >= notificationProperties.getHourlyLimit()) {
            throw new IllegalStateException("Hourly email limit reached");
        }
    }

    private void recordSend() {
        String hourKey = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0).toString();
        sentPerHour.merge(hourKey, 1, Integer::sum);
    }
}
