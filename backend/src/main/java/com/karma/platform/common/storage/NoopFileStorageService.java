package com.karma.platform.common.storage;

import com.karma.platform.common.ApiException;
import org.springframework.http.HttpStatus;

public class NoopFileStorageService implements FileStorageService {

    @Override
    public StoredFile upload(String key, byte[] content, String contentType) {
        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "storage.disabled", "File storage is not enabled");
    }

    @Override
    public void delete(String key) {
        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "storage.disabled", "File storage is not enabled");
    }
}
