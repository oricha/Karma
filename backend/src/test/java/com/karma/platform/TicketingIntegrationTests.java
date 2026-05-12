package com.karma.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karma.platform.model.OrderStatus;
import com.karma.platform.persistence.repository.OrderRepository;
import com.karma.platform.persistence.repository.TicketTypeRepository;
import com.karma.platform.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketingIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @MockBean
    private PaymentService paymentService;

    @Test
    void organizerCanManageTicketTypesAndCheckoutIsConfirmedByWebhook() throws Exception {
        AuthSession organizer = login("maria@karma.app", "password123");
        AuthSession attendee = login("demo@karma.app", "demo123");
        String eventId = createPaidEvent(organizer.accessToken());

        String ticketResponse = mockMvc.perform(post("/api/organizers/me/events/{id}/tickets", eventId)
                        .header("Authorization", "Bearer " + organizer.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Entrada General",
                                  "description": "Acceso completo",
                                  "price": 45.0,
                                  "currency": "EUR",
                                  "quantity": 30,
                                  "earlyBirdPrice": 39.0,
                                  "earlyBirdQuantity": 10,
                                  "earlyBirdDeadline": "2026-11-25T23:59:00",
                                  "saleStart": "2026-11-01T00:00:00",
                                  "saleEnd": "2026-12-20T20:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Entrada General"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String ticketTypeId = objectMapper.readTree(ticketResponse).get("id").asText();

        mockMvc.perform(put("/api/organizers/me/events/{id}/tickets/{ticketTypeId}", eventId, ticketTypeId)
                        .header("Authorization", "Bearer " + organizer.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Entrada General Plus",
                                  "description": "Acceso completo actualizado",
                                  "price": 50.0,
                                  "currency": "EUR",
                                  "quantity": 40,
                                  "earlyBirdPrice": 42.0,
                                  "earlyBirdQuantity": 8,
                                  "earlyBirdDeadline": "2026-11-20T23:59:00",
                                  "saleStart": "2026-11-01T00:00:00",
                                  "saleEnd": "2026-12-20T20:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Entrada General Plus"))
                .andExpect(jsonPath("$.quantity").value(40));

        mockMvc.perform(get("/api/organizers/me/events/{id}/tickets", eventId)
                        .header("Authorization", "Bearer " + organizer.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + ticketTypeId + "')].name").value(hasItem("Entrada General Plus")));

        when(paymentService.createCheckoutSession(any(), any(), anyList()))
                .thenReturn(new PaymentService.CheckoutSessionResult(
                        "cs_test_phase3",
                        "https://checkout.stripe.test/cs_test_phase3",
                        "pi_test_phase3"
                ));

        String checkoutResponse = mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + attendee.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "%s",
                                  "items": [
                                    {
                                      "ticketTypeId": "%s",
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """.formatted(eventId, ticketTypeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("cs_test_phase3"))
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.stripe.test/cs_test_phase3"))
                .andExpect(jsonPath("$.order.status").value("PENDING"))
                .andExpect(jsonPath("$.order.stripeSessionId").value("cs_test_phase3"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(checkoutResponse).get("order").get("id").asText();

        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .header("Authorization", "Bearer " + attendee.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.stripeSessionId").value("cs_test_phase3"));

        when(paymentService.parseWebhook(any(), any()))
                .thenReturn(new PaymentService.WebhookResult(
                        "checkout.session.completed",
                        "cs_test_phase3",
                        "pi_test_phase3"
                ));

        mockMvc.perform(post("/webhook/stripe/checkout-session")
                        .header("Stripe-Signature", "test-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"checkout.session.completed\"}"))
                .andExpect(status().isOk());

        var paidOrder = orderRepository.findById(orderId).orElseThrow();
        var updatedTicket = ticketTypeRepository.findById(ticketTypeId).orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.PAID, paidOrder.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals("pi_test_phase3", paidOrder.getStripePaymentIntentId());
        org.junit.jupiter.api.Assertions.assertEquals(2, updatedTicket.getSoldCount());
    }

    private String createPaidEvent(String organizerToken) throws Exception {
        String response = mockMvc.perform(post("/api/organizers/me/events")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupId": "group-1",
                                  "title": "Festival Sonoro %s",
                                  "description": "Evento de pago",
                                  "coverImageUrl": null,
                                  "startDate": "2026-12-21T18:00:00",
                                  "endDate": "2026-12-21T22:00:00",
                                  "venueName": "Centro Karma",
                                  "address": "Direccion 3",
                                  "city": "Madrid",
                                  "country": "Espana",
                                  "latitude": 40.4168,
                                  "longitude": -3.7038,
                                  "isOnline": false,
                                  "isHybrid": false,
                                  "onlineUrl": null,
                                  "status": "PUBLISHED",
                                  "featured": false,
                                  "maxAttendees": 100,
                                  "isFree": false,
                                  "price": 50.0,
                                  "currency": "EUR",
                                  "language": "es",
                                  "categoryId": "cat-music",
                                  "themeIds": ["theme-kirtan"],
                                  "remindersEnabled": true
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private AuthSession login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return new AuthSession(node.get("user").get("id").asText(), node.get("accessToken").asText());
    }

    private record AuthSession(String userId, String accessToken) {
    }
}
