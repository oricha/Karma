package com.karma.platform.common.storage;

import com.karma.platform.config.KarmaStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageOrphanCleanupService {

    private static final Logger log = LoggerFactory.getLogger(StorageOrphanCleanupService.class);
    private static final String UPLOAD_PREFIX = "uploads/";

    private final KarmaStorageProperties storageProperties;
    private final FileStorageService fileStorageService;
    private final StorageReferenceService storageReferenceService;

    public StorageOrphanCleanupService(
            KarmaStorageProperties storageProperties,
            FileStorageService fileStorageService,
            StorageReferenceService storageReferenceService
    ) {
        this.storageProperties = storageProperties;
        this.fileStorageService = fileStorageService;
        this.storageReferenceService = storageReferenceService;
    }

    public int deleteOrphanedUploads() {
        if (!storageProperties.enabled()) {
            return 0;
        }
        List<String> keys = fileStorageService.listKeys(UPLOAD_PREFIX);
        int deleted = 0;
        for (String key : keys) {
            if (!storageReferenceService.isKeyReferenced(key)) {
                fileStorageService.delete(key);
                deleted++;
                log.info("Deleted orphaned upload key={}", key);
            }
        }
        return deleted;
    }
}
