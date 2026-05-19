package com.karma.platform.common.storage;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public interface FileStorageService {

    StoredFile upload(String key, byte[] content, String contentType);

    void delete(String key);

    boolean objectExists(String key);

    PresignedUpload generatePresignedUploadUrl(String key, String contentType, Duration ttl);

    String generatePresignedDownloadUrl(String key, Duration ttl);

    default List<String> listKeys(String prefix) {
        return Collections.emptyList();
    }
}
