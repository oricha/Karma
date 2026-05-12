package com.karma.platform.common.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LocalizationService {

    private final MessageSource messageSource;

    public LocalizationService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String message(String code, Object... args) {
        return message(code, LocaleContextHolder.getLocale(), args);
    }

    public String message(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, code, locale);
    }
}
