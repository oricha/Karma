package com.karma.platform.service.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "karma.schedulers.digest-enabled", havingValue = "true", matchIfMissing = true)
public class DigestScheduler {

    private final DigestService digestService;

    public DigestScheduler(DigestService digestService) {
        this.digestService = digestService;
    }

    @Scheduled(cron = "${karma.schedulers.digest-cron:0 0 9 * * MON}")
    public void sendDigests() {
        digestService.sendEligibleDigests();
    }
}
