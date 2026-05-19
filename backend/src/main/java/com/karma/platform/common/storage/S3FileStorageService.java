package com.karma.platform.common.storage;

import com.karma.platform.config.KarmaStorageProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final KarmaStorageProperties properties;

    public S3FileStorageService(S3Client s3Client, S3Presigner presigner, KarmaStorageProperties properties) {
        this.s3Client = s3Client;
        this.presigner = presigner;
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

    @Override
    public boolean objectExists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException exception) {
            return false;
        }
    }

    @Override
    public PresignedUpload generatePresignedUploadUrl(String key, String contentType, Duration ttl) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .build();
        var presigned = presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .putObjectRequest(putRequest)
                .build());
        return new PresignedUpload(key, presigned.url().toString(), contentType, properties.maxUploadSizeBytes());
    }

    @Override
    public String generatePresignedDownloadUrl(String key, Duration ttl) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .build();
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(ttl)
                        .getObjectRequest(getRequest)
                        .build())
                .url()
                .toString();
    }

    @Override
    public List<String> listKeys(String prefix) {
        List<String> keys = new ArrayList<>();
        String continuationToken = null;
        do {
            var response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                    .bucket(properties.bucket())
                    .prefix(prefix)
                    .continuationToken(continuationToken)
                    .build());
            for (S3Object object : response.contents()) {
                if (object.key() != null && !object.key().endsWith("/")) {
                    keys.add(object.key());
                }
            }
            continuationToken = response.nextContinuationToken();
        } while (continuationToken != null);
        return keys;
    }

    private String resolveUrl(String key) {
        if (properties.publicBaseUrl() != null && !properties.publicBaseUrl().isBlank()) {
            return properties.publicBaseUrl().replaceAll("/$", "") + "/" + key;
        }
        return "s3://" + properties.bucket() + "/" + key;
    }
}
