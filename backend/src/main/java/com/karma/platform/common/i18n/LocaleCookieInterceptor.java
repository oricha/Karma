package com.karma.platform.common.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

public class LocaleCookieInterceptor implements HandlerInterceptor {

    private final LocaleResolver localeResolver;

    public LocaleCookieInterceptor(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String language = request.getParameter("lang");
        if (StringUtils.hasText(language)) {
            localeResolver.setLocale(request, response, Locale.forLanguageTag(language));
        }
        return true;
    }
}
