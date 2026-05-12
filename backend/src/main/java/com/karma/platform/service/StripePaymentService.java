package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.config.StripeProperties;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.OrderEntity;
import com.karma.platform.persistence.entity.OrderItemEntity;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class StripePaymentService implements PaymentService {

    private final StripeProperties stripeProperties;

    public StripePaymentService(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
    }

    @Override
    public CheckoutSessionResult createCheckoutSession(OrderEntity order, EventEntity event, List<OrderItemEntity> orderItems) {
        requireApiKey();
        Stripe.apiKey = stripeProperties.getApiKey();
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeProperties.getSuccessUrl())
                .setCancelUrl(stripeProperties.getCancelUrl())
                .putMetadata("orderId", order.getId())
                .putMetadata("eventId", event.getId());
        orderItems.forEach(item -> builder.addLineItem(toLineItem(item)));
        try {
            Session session = Session.create(builder.build());
            return new CheckoutSessionResult(session.getId(), session.getUrl(), session.getPaymentIntent());
        } catch (StripeException exception) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "error.payment-provider-unavailable", "Payment provider unavailable");
        }
    }

    @Override
    public WebhookResult parseWebhook(String payload, String signatureHeader) {
        if (!StringUtils.hasText(stripeProperties.getWebhookSecret())) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "error.payment-webhook-misconfigured", "Payment webhook misconfigured");
        }
        try {
            Event event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.getWebhookSecret());
            if (!"checkout.session.completed".equals(event.getType())) {
                return new WebhookResult(event.getType(), null, null);
            }
            Session session = (Session) event.getDataObjectDeserializer().getObject()
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "error.payment-webhook-invalid", "Payment webhook invalid"));
            return new WebhookResult(event.getType(), session.getId(), session.getPaymentIntent());
        } catch (SignatureVerificationException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.payment-webhook-invalid-signature", "Payment webhook signature invalid");
        }
    }

    private SessionCreateParams.LineItem toLineItem(OrderItemEntity item) {
        long unitAmount = Math.round(item.getUnitPrice() * 100);
        return SessionCreateParams.LineItem.builder()
                .setQuantity((long) item.getQuantity())
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(item.getCurrency().toLowerCase())
                                .setUnitAmount(unitAmount)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(item.getTicketName())
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    private void requireApiKey() {
        if (!StringUtils.hasText(stripeProperties.getApiKey())) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "error.payment-provider-unavailable", "Payment provider unavailable");
        }
    }
}
