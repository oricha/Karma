package com.karma.platform.config;

import com.karma.platform.common.storage.FileStorageService;
import com.karma.platform.common.storage.FileUploadValidator;
import com.karma.platform.common.storage.NoopFileStorageService;
import com.karma.platform.common.storage.S3FileStorageService;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class StorageConfig {

    @Bean
    FileUploadValidator fileUploadValidator(KarmaStorageProperties properties, MessageSource messageSource) {
        return new FileUploadValidator(properties, messageSource);
    }

    @Bean
    FileStorageService fileStorageService(KarmaStorageProperties properties) {
        if (!properties.enabled()) {
            return new NoopFileStorageService();
        }

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.region()))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

        if (properties.endpoint() != null && !properties.endpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.endpoint()));
        }

        if (properties.accessKey() != null && !properties.accessKey().isBlank()
                && properties.secretKey() != null && !properties.secretKey().isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())
            ));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        S3Client s3Client = builder.build();
        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .region(Region.of(properties.region()))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        if (properties.endpoint() != null && !properties.endpoint().isBlank()) {
            presignerBuilder.endpointOverride(URI.create(properties.endpoint()));
        }
        if (properties.accessKey() != null && !properties.accessKey().isBlank()
                && properties.secretKey() != null && !properties.secretKey().isBlank()) {
            presignerBuilder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())
            ));
        } else {
            presignerBuilder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return new S3FileStorageService(s3Client, presignerBuilder.build(), properties);
    }
}
