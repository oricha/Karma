package com.karma.platform.config;

import com.karma.platform.service.notification.EmailDailyQuotaService;
import com.karma.platform.service.notification.EmailDeferredQueueService;
import com.karma.platform.service.notification.EmailTemplateRenderer;
import com.karma.platform.service.notification.EmailService;
import com.karma.platform.service.notification.LocalSmtpEmailService;
import com.karma.platform.service.notification.NoopEmailService;
import com.karma.platform.service.notification.SendGridEmailService;
import com.sendgrid.SendGrid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.thymeleaf.TemplateEngine;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({NotificationProperties.class, NotificationEmailProperties.class})
public class NotificationConfiguration {

    @Bean
    EmailTemplateRenderer emailTemplateRenderer(TemplateEngine templateEngine, MessageSource messageSource) {
        return new EmailTemplateRenderer(templateEngine, messageSource);
    }

    @Bean
    @ConditionalOnProperty(name = "karma.email.provider", havingValue = "sendgrid", matchIfMissing = true)
    SendGrid sendGrid(NotificationEmailProperties properties) {
        return new SendGrid(properties.getApiKey());
    }

    @Bean
    @ConditionalOnExpression("'${karma.email.enabled:false}' == 'true' && '${karma.email.provider:sendgrid}' == 'sendgrid'")
    EmailService sendGridEmailService(
            SendGrid sendGrid,
            EmailTemplateRenderer emailTemplateRenderer,
            NotificationEmailProperties emailProperties,
            NotificationProperties notificationProperties,
            EmailDailyQuotaService dailyQuotaService,
            EmailDeferredQueueService deferredQueueService
    ) {
        return new SendGridEmailService(
                sendGrid,
                emailTemplateRenderer,
                emailProperties,
                notificationProperties,
                dailyQuotaService,
                deferredQueueService
        );
    }

    @Bean
    @ConditionalOnExpression("'${karma.email.enabled:false}' == 'true' && '${karma.email.provider:sendgrid}' == 'mailhog'")
    EmailService localSmtpEmailService(
            JavaMailSender mailSender,
            EmailTemplateRenderer emailTemplateRenderer,
            NotificationEmailProperties emailProperties,
            EmailDailyQuotaService dailyQuotaService,
            EmailDeferredQueueService deferredQueueService
    ) {
        return new LocalSmtpEmailService(
                mailSender,
                emailTemplateRenderer,
                emailProperties,
                dailyQuotaService,
                deferredQueueService
        );
    }

    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    EmailService noopEmailService() {
        return new NoopEmailService();
    }
}
