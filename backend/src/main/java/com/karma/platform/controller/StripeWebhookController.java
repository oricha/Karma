package com.karma.platform.controller;

import com.karma.platform.service.OrderService;
import com.karma.platform.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook/stripe")
public class StripeWebhookController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    public StripeWebhookController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @PostMapping("/checkout-session")
    public ResponseEntity<Void> handleCheckoutSessionCompleted(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signatureHeader
    ) {
        PaymentService.WebhookResult event = paymentService.parseWebhook(payload, signatureHeader);
        if ("checkout.session.completed".equals(event.eventType()) && event.sessionId() != null) {
            orderService.confirmPayment(event.sessionId(), event.paymentIntentId());
        }
        return ResponseEntity.ok().build();
    }
}
