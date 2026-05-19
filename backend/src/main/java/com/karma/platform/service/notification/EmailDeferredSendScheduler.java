package com.karma.platform.service.notification;

import com.karma.platform.config.NotificationEmailProperties;
import com.karma.platform.persistence.entity.EmailDeferredSendEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

/**
 * Flushes deferred emails after daily quota reset (midnight UTC window).
 */
@Component
@ConditionalOnExpression("'${karma.email.enabled:false}' == 'true'")
public class EmailDeferredSendScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmailDeferredSendScheduler.class);

    private final EmailDeferredQueueService deferredQueueService;
    private final EmailDailyQuotaService dailyQuotaService;
    private final NotificationEmailProperties emailProperties;
    private final SendGrid sendGrid;
    private final JavaMailSender mailSender;
    private final String provider;

    public EmailDeferredSendScheduler(
            EmailDeferredQueueService deferredQueueService,
            EmailDailyQuotaService dailyQuotaService,
            NotificationEmailProperties emailProperties,
            @org.springframework.beans.factory.annotation.Autowired(required = false) SendGrid sendGrid,
            @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender,
            @org.springframework.beans.factory.annotation.Value("${karma.email.provider:sendgrid}") String provider
    ) {
        this.deferredQueueService = deferredQueueService;
        this.dailyQuotaService = dailyQuotaService;
        this.emailProperties = emailProperties;
        this.sendGrid = sendGrid;
        this.mailSender = mailSender;
        this.provider = provider == null ? "sendgrid" : provider;
    }

    @Scheduled(cron = "5 0 0 * * *")
    public void flushDeferredEmails() {
        for (EmailDeferredSendEntity row : deferredQueueService.dueSends()) {
            EmailPriority priority = EmailPriority.valueOf(row.getPriority());
            if (!dailyQuotaService.tryConsume(priority)) {
                log.warn("Deferred email still blocked by quota: {}", row.getId());
                continue;
            }
            try {
                dispatch(row.getSubject(), row.getRecipientEmail(), row.getHtmlBody());
                deferredQueueService.markSent(row);
            } catch (Exception exception) {
                log.error("Failed to flush deferred email {}", row.getId(), exception);
            }
        }
    }

    private void dispatch(String subject, String recipientEmail, String body) throws Exception {
        if ("mailhog".equalsIgnoreCase(provider)) {
            if (mailSender == null) {
                throw new IllegalStateException("JavaMailSender not configured for mailhog provider");
            }
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailProperties.getFromEmail(), emailProperties.getFromName());
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            return;
        }
        if (sendGrid == null) {
            throw new IllegalStateException("SendGrid client not configured");
        }
        Mail mail = new Mail(
                new Email(emailProperties.getFromEmail(), emailProperties.getFromName()),
                subject,
                new Email(recipientEmail),
                new Content("text/html", body)
        );
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sendGrid.api(request);
        if (response.getStatusCode() >= 400) {
            throw new IllegalStateException("SendGrid rejected deferred send with status " + response.getStatusCode());
        }
    }
}
