package com.karma.platform.service.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "karma.schedulers.review-request-enabled", havingValue = "true")
public class ReviewRequestScheduler {

    private final PersistentReviewRequestService reviewRequestService;

    public ReviewRequestScheduler(PersistentReviewRequestService reviewRequestService) {
        this.reviewRequestService = reviewRequestService;
    }

    @Scheduled(cron = "${karma.schedulers.review-request-cron:0 30 * * * *}")
    public void sendReviewRequests() {
        reviewRequestService.sendPendingReviewRequests();
    }
}
