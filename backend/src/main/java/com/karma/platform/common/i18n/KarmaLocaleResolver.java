package com.karma.platform.common.i18n;

import com.karma.platform.config.KarmaI18nProperties;
import com.karma.platform.model.User;
import com.karma.platform.seed.PlatformDataStore;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public class KarmaLocaleResolver implements LocaleResolver {

    private static final String LANGUAGE_PARAMETER = "lang";

    private final PlatformDataStore dataStore;
    private final KarmaI18nProperties properties;

    public KarmaLocaleResolver(PlatformDataStore dataStore, KarmaI18nProperties properties) {
        this.dataStore = dataStore;
        this.properties = properties;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return localeFromUser(request)
                .or(() -> localeFromParam(request))
                .or(() -> localeFromCookie(request))
                .or(() -> localeFromHeader(request))
                .orElse(defaultLocale());
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        Cookie cookie = new Cookie(properties.localeCookieName(), locale.toLanguageTag());
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365);
        response.addCookie(cookie);
    }

    private Optional<Locale> localeFromUser(HttpServletRequest request) {
        Object principal = request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName();
        if (principal == null) {
            return Optional.empty();
        }
        return dataStore.findUserById(principal.toString())
                .map(User::locale)
                .flatMap(this::parseLocale);
    }

    private Optional<Locale> localeFromParam(HttpServletRequest request) {
        return parseLocale(request.getParameter(LANGUAGE_PARAMETER));
    }

    private Optional<Locale> localeFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> properties.localeCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .flatMap(this::parseLocale);
    }

    private Optional<Locale> localeFromHeader(HttpServletRequest request) {
        return parseLocale(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE));
    }

    private Optional<Locale> parseLocale(String value) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        Locale locale = Locale.forLanguageTag(value.replace('_', '-').split(",")[0].trim());
        if (!StringUtils.hasText(locale.getLanguage())) {
            return Optional.empty();
        }
        return Optional.of(locale);
    }

    private Locale defaultLocale() {
        return Locale.forLanguageTag(properties.defaultLocale());
    }
}
