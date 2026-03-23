package com.karma.platform.model;

import java.time.LocalDateTime;

public record Order(
        String id,
        String userId,
        String eventId,
        OrderStatus status,
        double totalAmount,
        String currency,
        LocalDateTime purchasedAt
) {
}
