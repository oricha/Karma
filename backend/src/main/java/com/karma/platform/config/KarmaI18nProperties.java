package com.karma.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "karma.i18n")
public record KarmaI18nProperties(
        String defaultLocale,
        String localeCookieName
) {
}
