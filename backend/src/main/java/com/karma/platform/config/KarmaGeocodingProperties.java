package com.karma.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "karma.geocoding")
public record KarmaGeocodingProperties(
        boolean enabled,
        String provider,
        String baseUrl,
        String userAgent,
        int timeoutMillis,
        int maxRetries,
        int cacheSize
) {
}
