package com.karma.platform.common.storage;

public interface FileStorageService {

    StoredFile upload(String key, byte[] content, String contentType);

    void delete(String key);
}
