package com.karma.platform.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class EmailRetryTemplate {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 1000;
    private static final Logger log = LoggerFactory.getLogger(EmailRetryTemplate.class);

    public static <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        int attempt = 1;
        long delay = INITIAL_DELAY_MS;

        while (attempt <= MAX_RETRIES) {
            try {
                log.debug("Attempt {} of {} for operation: {}", attempt, MAX_RETRIES, operationName);
                return operation.get();
            } catch (Exception e) {
                if (attempt == MAX_RETRIES) {
                    log.error("Failed to execute {} after {} retries", operationName, MAX_RETRIES, e);
                    throw new RuntimeException("Failed to execute " + operationName + " after " + MAX_RETRIES + " attempts", e);
                }

                log.warn("Attempt {} failed for operation: {}, retrying in {} ms. Error: {}",
                        attempt, operationName, delay, e.getMessage());

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }

                delay = (long) (delay * 1.5);
                attempt++;
            }
        }

        throw new RuntimeException("Unexpected error in retry template");
    }

    public static void executeWithRetry(Runnable operation, String operationName) {
        executeWithRetry(() -> {
            operation.run();
            return null;
        }, operationName);
    }
}
