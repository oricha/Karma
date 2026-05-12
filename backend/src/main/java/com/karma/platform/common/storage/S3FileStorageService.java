package com.karma.platform.common.storage;

import com.karma.platform.config.KarmaStorageProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final KarmaStorageProperties properties;

    public S3FileStorageService(S3Client s3Client, KarmaStorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public StoredFile upload(String key, byte[] content, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(content));
        return new StoredFile(properties.bucket(), key, contentType, content.length, resolveUrl(key));
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .build());
    }

    private String resolveUrl(String key) {
        if (properties.publicBaseUrl() != null && !properties.publicBaseUrl().isBlank()) {
            return properties.publicBaseUrl().replaceAll("/$", "") + "/" + key;
        }
        return "s3://" + properties.bucket() + "/" + key;
    }
}
