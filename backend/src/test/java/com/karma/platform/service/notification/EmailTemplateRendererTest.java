package com.karma.platform.service.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailTemplateRendererTest {

    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private MessageSource messageSource;

    private EmailTemplateRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new EmailTemplateRenderer(templateEngine, messageSource);
    }

    @Test
    void fallsBackFromCatalanToSpanishTemplate() {
        when(templateEngine.process(eq(EmailTemplate.WELCOME.getTemplatePathCa()), any(Context.class)))
                .thenThrow(new TemplateInputException("missing"));
        when(templateEngine.process(eq(EmailTemplate.WELCOME.getTemplatePathEs()), any(Context.class)))
                .thenReturn("<p>Hola</p>");

        String html = renderer.render(EmailTemplate.WELCOME, EmailTemplateRenderer.UserLocale.CA, Map.of());

        assertEquals("<p>Hola</p>", html);
    }

    @Test
    void resolvesCatalanSubject() {
        when(messageSource.getMessage(eq("email.welcome.subject"), eq(null), eq("email.welcome.subject"),
                eq(Locale.forLanguageTag("ca"))))
                .thenReturn("Benvingut");

        String subject = renderer.subject("email.welcome.subject", EmailTemplateRenderer.UserLocale.CA);

        assertEquals("Benvingut", subject);
    }
}
