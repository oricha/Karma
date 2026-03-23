package com.karma.platform.dto;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record OrderResponse(
            String id,
            String userId,
            String eventId,
            EventDtos.EventResponse event,
            String status,
            double totalAmount,
            String currency,
            String purchasedAt
    ) {
    }

    public record CheckoutResponse(
            String checkoutUrl,
            OrderResponse order
    ) {
    }
}
