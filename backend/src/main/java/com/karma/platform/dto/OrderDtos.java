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
            String purchasedAt,
            String stripeSessionId
    ) {
    }

    public record CheckoutItemRequest(
            String ticketTypeId,
            int quantity
    ) {
    }

    public record CheckoutRequest(
            String eventId,
            java.util.List<CheckoutItemRequest> items
    ) {
    }

    public record CheckoutResponse(
            String sessionId,
            String checkoutUrl,
            OrderResponse order
    ) {
    }

    public record TicketTypeResponse(
            String id,
            String eventId,
            String name,
            String description,
            double price,
            String currency,
            int quantity,
            int soldCount,
            Double earlyBirdPrice,
            Integer earlyBirdQuantity,
            String earlyBirdDeadline,
            String saleStart,
            String saleEnd
    ) {
    }

    public record UpsertTicketTypeRequest(
            String name,
            String description,
            double price,
            String currency,
            int quantity,
            Double earlyBirdPrice,
            Integer earlyBirdQuantity,
            java.time.LocalDateTime earlyBirdDeadline,
            java.time.LocalDateTime saleStart,
            java.time.LocalDateTime saleEnd
    ) {
    }
}
