package com.karma.platform.service;

import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.OrderEntity;
import com.karma.platform.persistence.entity.OrderItemEntity;

import java.util.List;

public interface PaymentService {

    CheckoutSessionResult createCheckoutSession(OrderEntity order, EventEntity event, List<OrderItemEntity> orderItems);

    WebhookResult parseWebhook(String payload, String signatureHeader);

    record CheckoutSessionResult(
            String sessionId,
            String checkoutUrl,
            String paymentIntentId
    ) {
    }

    record WebhookResult(
            String eventType,
            String sessionId,
            String paymentIntentId
    ) {
    }
}
