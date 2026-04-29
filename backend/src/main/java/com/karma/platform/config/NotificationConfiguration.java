package com.karma.platform.config;

import com.karma.platform.service.notification.EmailService;
import com.karma.platform.service.notification.NoopEmailService;
import com.karma.platform.service.notification.SendGridEmailService;
import com.sendgrid.SendGrid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.thymeleaf.TemplateEngine;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({NotificationProperties.class, NotificationEmailProperties.class})
public class NotificationConfiguration {

    @Bean
    @ConditionalOnProperty(name = "karma.email.enabled", havingValue = "true")
    SendGrid sendGrid(NotificationEmailProperties properties) {
        return new SendGrid(properties.getApiKey());
    }

    @Bean
    @ConditionalOnProperty(name = "karma.email.enabled", havingValue = "true")
    EmailService sendGridEmailService(
            SendGrid sendGrid,
            TemplateEngine templateEngine,
            MessageSource messageSource,
            NotificationEmailProperties emailProperties,
            NotificationProperties notificationProperties
    ) {
        return new SendGridEmailService(sendGrid, templateEngine, messageSource, emailProperties, notificationProperties);
    }

    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    EmailService noopEmailService() {
        return new NoopEmailService();
    }
}
