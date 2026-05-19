package com.karma.platform.common.storage;

public record PresignedUpload(
        String key,
        String uploadUrl,
        String contentType,
        long maxSizeBytes
) {
}
