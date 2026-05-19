package com.karma.platform.common.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "karma.schedulers.storage-orphan-cleanup-enabled", havingValue = "true")
public class StorageOrphanCleanupScheduler {

    private final StorageOrphanCleanupService orphanCleanupService;

    public StorageOrphanCleanupScheduler(StorageOrphanCleanupService orphanCleanupService) {
        this.orphanCleanupService = orphanCleanupService;
    }

    @Scheduled(cron = "${karma.schedulers.storage-orphan-cleanup-cron:0 0 3 * * *}")
    public void cleanupOrphanedUploads() {
        orphanCleanupService.deleteOrphanedUploads();
    }
}
