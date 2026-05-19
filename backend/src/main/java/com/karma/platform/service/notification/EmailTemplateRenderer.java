package com.karma.platform.service.notification;

import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

import java.util.Locale;
import java.util.Map;

/**
 * Resolves Thymeleaf email templates with locale fallback: requested → ES → EN.
 */
public class EmailTemplateRenderer {

    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;

    public EmailTemplateRenderer(TemplateEngine templateEngine, MessageSource messageSource) {
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
    }

    public String render(EmailTemplate template, UserLocale userLocale, Map<String, Object> variables) {
        Context context = new Context(toJavaLocale(userLocale));
        context.setVariables(variables);
        for (UserLocale candidate : UserLocale.fallbackChain(userLocale)) {
            String path = templatePath(template, candidate);
            try {
                return templateEngine.process(path, context);
            } catch (TemplateInputException ignored) {
                // try next locale in chain
            }
        }
        return templateEngine.process(template.getTemplatePathEs(), context);
    }

    public String subject(String key, UserLocale userLocale) {
        Locale locale = toJavaLocale(userLocale);
        return messageSource.getMessage(key, null, key, locale);
    }

    public static UserLocale userLocale(String localeCode) {
        if (localeCode == null) {
            return UserLocale.ES;
        }
        return switch (localeCode.trim().toLowerCase(Locale.ROOT)) {
            case "en" -> UserLocale.EN;
            case "ca" -> UserLocale.CA;
            default -> UserLocale.ES;
        };
    }

    private static String templatePath(EmailTemplate template, UserLocale locale) {
        return switch (locale) {
            case EN -> template.getTemplatePathEn();
            case CA -> template.getTemplatePathCa();
            case ES -> template.getTemplatePathEs();
        };
    }

    private static Locale toJavaLocale(UserLocale locale) {
        return switch (locale) {
            case EN -> Locale.ENGLISH;
            case CA -> Locale.forLanguageTag("ca");
            case ES -> Locale.forLanguageTag("es");
        };
    }

    public enum UserLocale {
        ES, EN, CA;

        static UserLocale[] fallbackChain(UserLocale requested) {
            return switch (requested) {
                case CA -> new UserLocale[]{CA, ES, EN};
                case EN -> new UserLocale[]{EN, ES};
                case ES -> new UserLocale[]{ES, EN};
            };
        }
    }
}
