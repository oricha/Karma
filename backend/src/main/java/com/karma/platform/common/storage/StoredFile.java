package com.karma.platform.common.storage;

public record StoredFile(
        String bucket,
        String key,
        String contentType,
        long size,
        String url
) {
}
