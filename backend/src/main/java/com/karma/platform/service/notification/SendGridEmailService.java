package com.karma.platform.service.notification;

import com.karma.platform.entity.User;
import com.karma.platform.entity.Event;
import com.karma.platform.entity.Order;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.Email;
import com.sendgrid.helpers.mail.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendGridEmailService implements EmailService {

    private final SendGrid sendGrid;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;

    @Value("${sendgrid.from-email:noreply@karma.local}")
    private String fromEmail;

    @Value("${sendgrid.from-name:Karma}")
    private String fromName;

    @Value("${app.unsubscribe-base-url:https://karma.local}")
    private String unsubscribeBaseUrl;

    private static final int RATE_LIMIT_PER_HOUR = 500;
    private final Map<String, HourlyEmailCount> emailCounts = new ConcurrentHashMap<>();

    private static class HourlyEmailCount {
        LocalDateTime hourStart;
        AtomicInteger count = new AtomicInteger(0);

        HourlyEmailCount(LocalDateTime hourStart) {
            this.hourStart = hourStart;
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(User user) {
        String subject = messageSource.getMessage("email.welcome.subject", null, getLocale(user));
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        sendEmail(user.getEmail(), subject, EmailTemplate.WELCOME, variables, user.getLocale());
    }

    @Override
    @Async
    public void sendEmailVerificationEmail(User user, String verificationCode) {
        String subject = messageSource.getMessage("email.verification.subject", null, getLocale(user));
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("verificationCode", verificationCode);
        variables.put("verificationLink", buildVerificationLink(user.getId(), verificationCode));
        sendEmail(user.getEmail(), subject, EmailTemplate.EMAIL_VERIFICATION, variables, user.getLocale());
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        String subject = messageSource.getMessage("email.password-reset.subject", null, getLocale(user));
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("resetToken", resetToken);
        variables.put("resetLink", buildPasswordResetLink(user.getId(), resetToken));
        sendEmail(user.getEmail(), subject, EmailTemplate.PASSWORD_RESET, variables, user.getLocale());
    }

    @Override
    @Async
    public void sendRsvpConfirmationEmail(User user, Event event) {
        String subject = messageSource.getMessage("email.rsvp-confirmation.subject", null, getLocale(user));
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("event", event);
        sendEmail(user.getEmail(), subject, EmailTemplate.RSVP_CONFIRMATION, variables, user.getLocale());
    }

    @Override
    @Async
    public void sendWaitlistPromotionEmail(User user, Event event) {
        String subject = messageSource.getMessage("email.waitlist-promotion.subject", null, getLocale(user));
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("event", event);
        sendEmail(user.getEmail(), subject, EmailTemplate.WAITLIST_PROMOTION, variables, user.getLocale());
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(User user, Order order) {
        String subject = messageSource.getMessage("email.order-confirmation.subject", null, getLocale(user));
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("order", order);
        sendEmail(user.getEmail(), subject, EmailTemplate.ORDER_CONFIRMATION, variables, user.getLocale());
    }

    @Override
    @Async
    public void sendEventCancellationEmail(User user, Event event) {
        String subject = messageSource.getMessage("email.event-cancellation.subject", null, getLocale(user));
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("event", event);
        sendEmail(user.getEmail(), subject, EmailTemplate.EVENT_CANCELLATION, variables, user.getLocale());
    }

    @Override
    @Async
    public void sendReviewRequestEmail(User user, Event event) {
        String subject = messageSource.getMessage("email.review-request.subject", null, getLocale(user));
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("event", event);
        sendEmail(user.getEmail(), subject, EmailTemplate.REVIEW_REQUEST, variables, user.getLocale());
    }

    @Override
    @Async
    public void sendNewEventNotificationEmail(User user, Event event, String groupName) {
        String subject = messageSource.getMessage("email.new-group-event.subject", null, getLocale(user));
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("event", event);
        variables.put("groupName", groupName);
        sendEmail(user.getEmail(), subject, EmailTemplate.NEW_GROUP_EVENT, variables, user.getLocale());
    }

    @Override
    @Async
    public void sendWeeklyDigestEmail(User user, DigestContent digest) {
        String subject = messageSource.getMessage("email.weekly-digest.subject", null, getLocale(user));
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("digest", digest);
        variables.put("unsubscribeToken", generateUnsubscribeToken(user.getId()));
        sendEmail(user.getEmail(), subject, EmailTemplate.WEEKLY_DIGEST, variables, user.getLocale());
    }

    @Override
    @Async
    public void sendEventReminderEmail(User user, Event event, ReminderType reminderType) {
        String templateKey = switch (reminderType) {
            case SEVEN_DAY -> "email.event-reminder-7day.subject";
            case ONE_DAY -> "email.event-reminder-1day.subject";
            case TWO_HOUR -> "email.event-reminder-2hour.subject";
        };
        String subject = messageSource.getMessage(templateKey, null, getLocale(user));
        EmailTemplate template = switch (reminderType) {
            case SEVEN_DAY -> EmailTemplate.EVENT_REMINDER_7DAY;
            case ONE_DAY -> EmailTemplate.EVENT_REMINDER_1DAY;
            case TWO_HOUR -> EmailTemplate.EVENT_REMINDER_2HOUR;
        };
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("event", event);
        variables.put("reminderType", reminderType);
        sendEmail(user.getEmail(), subject, template, variables, user.getLocale());
    }

    @Override
    @Async
    public void sendPlatformNewsEmail(User user, List<BlogPostSummary> featuredPosts) {
        String subject = messageSource.getMessage("email.platform-news.subject", null, getLocale(user));
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("featuredPosts", featuredPosts);
        variables.put("unsubscribeToken", generateUnsubscribeToken(user.getId()));
        sendEmail(user.getEmail(), subject, EmailTemplate.PLATFORM_NEWS, variables, user.getLocale());
    }

    private void sendEmail(String to, String subject, EmailTemplate template, Map<String, Object> variables, String locale) {
        EmailRetryTemplate.executeWithRetry(() -> {
            checkRateLimit();

            String htmlContent = renderTemplate(template, variables, locale);
            Email from = new Email(fromEmail, fromName);
            Email recipient = new Email(to);
            Content content = new Content("text/html", htmlContent);

            Mail mail = new Mail(from, subject, recipient, content);
            mail.setReplyTo(new Email(fromEmail));

            var response = sendGrid.api(new com.sendgrid.Request());
            response.setMethod(com.sendgrid.Request.Method.POST);
            response.setEndpoint("mail/send");
            response.setBody(mail.build());

            sendGrid.api(response);
            recordEmailSent();
            log.info("Email sent to {} with template {}", to, template);
        }, "send email to " + to);
    }

    private String renderTemplate(EmailTemplate template, Map<String, Object> variables, String locale) {
        Context context = new Context(new Locale(locale.toLowerCase()));
        context.setVariables(variables);

        String templatePath = locale.equalsIgnoreCase("ES") ? template.getTemplatePathEs() : template.getTemplatePathEn();
        return templateEngine.process(templatePath, context);
    }

    private void checkRateLimit() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hourStart = now.withMinute(0).withSecond(0).withNano(0);

        HourlyEmailCount count = emailCounts.compute(hourStart.toString(), (k, v) -> {
            if (v == null || !v.hourStart.equals(hourStart)) {
                return new HourlyEmailCount(hourStart);
            }
            return v;
        });

        if (count.count.get() >= RATE_LIMIT_PER_HOUR) {
            throw new RuntimeException("Rate limit exceeded: max 500 emails per hour");
        }
    }

    private void recordEmailSent() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hourStart = now.withMinute(0).withSecond(0).withNano(0);
        HourlyEmailCount count = emailCounts.get(hourStart.toString());
        if (count != null) {
            count.count.incrementAndGet();
        }
    }

    private String buildVerificationLink(String userId, String code) {
        return unsubscribeBaseUrl + "/api/auth/verify?userId=" + userId + "&code=" + code;
    }

    private String buildPasswordResetLink(String userId, String token) {
        return unsubscribeBaseUrl + "/api/auth/reset-password?userId=" + userId + "&token=" + token;
    }

    private String generateUnsubscribeToken(String userId) {
        return java.util.Base64.getEncoder().encodeToString((userId + ":" + System.currentTimeMillis()).getBytes());
    }

    private Locale getLocale(User user) {
        return new Locale(user.getLocale() != null ? user.getLocale().toLowerCase() : "en");
    }
}
