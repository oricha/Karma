package com.karma.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "karma.storage")
public record KarmaStorageProperties(
        boolean enabled,
        String bucket,
        String region,
        String endpoint,
        String accessKey,
        String secretKey,
        String publicBaseUrl,
        long maxUploadSizeBytes
) {
}
