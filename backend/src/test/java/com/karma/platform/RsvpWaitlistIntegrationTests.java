package com.karma.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RsvpWaitlistIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cancellationReordersWaitlistAndPromotesNextAttendee() throws Exception {
        AuthSession organizer = login("maria@karma.app", "password123");
        AuthSession firstAttendee = login("demo@karma.app", "demo123");
        AuthSession secondAttendee = register("waitlist-second");
        AuthSession thirdAttendee = register("waitlist-third");

        String eventId = createLimitedEvent(organizer.accessToken());

        mockMvc.perform(post("/api/events/{id}/rsvp", eventId)
                        .header("Authorization", "Bearer " + firstAttendee.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("YES"));

        mockMvc.perform(post("/api/events/{id}/rsvp", eventId)
                        .header("Authorization", "Bearer " + secondAttendee.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITLISTED"))
                .andExpect(jsonPath("$.waitlistPosition").value(1));

        mockMvc.perform(post("/api/events/{id}/rsvp", eventId)
                        .header("Authorization", "Bearer " + thirdAttendee.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITLISTED"))
                .andExpect(jsonPath("$.waitlistPosition").value(2));

        mockMvc.perform(delete("/api/events/{id}/rsvp", eventId)
                        .header("Authorization", "Bearer " + secondAttendee.accessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/events/{id}/rsvp", eventId)
                        .header("Authorization", "Bearer " + thirdAttendee.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITLISTED"))
                .andExpect(jsonPath("$.waitlistPosition").value(1));

        mockMvc.perform(delete("/api/events/{id}/rsvp", eventId)
                        .header("Authorization", "Bearer " + firstAttendee.accessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/events/{id}/rsvp", eventId)
                        .header("Authorization", "Bearer " + thirdAttendee.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("YES"))
                .andExpect(jsonPath("$.waitlistPosition").doesNotExist());

        mockMvc.perform(post("/api/organizers/me/events/{id}/checkin", eventId)
                        .header("Authorization", "Bearer " + organizer.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s"
                                }
                                """.formatted(thirdAttendee.userId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedIn").value(true))
                .andExpect(jsonPath("$.noShow").value(false));

        mockMvc.perform(post("/api/organizers/me/events/{id}/no-show", eventId)
                        .header("Authorization", "Bearer " + organizer.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s"
                                }
                                """.formatted(thirdAttendee.userId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedIn").value(false))
                .andExpect(jsonPath("$.noShow").value(true));
    }

    private String createLimitedEvent(String organizerToken) throws Exception {
        String response = mockMvc.perform(post("/api/organizers/me/events")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupId": "group-1",
                                  "title": "Evento Cupo Limitado %s",
                                  "description": "Prueba waitlist",
                                  "coverImageUrl": null,
                                  "startDate": "2026-11-20T18:00:00",
                                  "endDate": "2026-11-20T20:00:00",
                                  "venueName": "Sala Karma",
                                  "address": "Direccion 2",
                                  "city": "Madrid",
                                  "country": "Espana",
                                  "latitude": 40.4168,
                                  "longitude": -3.7038,
                                  "isOnline": false,
                                  "isHybrid": false,
                                  "onlineUrl": null,
                                  "status": "PUBLISHED",
                                  "featured": false,
                                  "maxAttendees": 1,
                                  "isFree": true,
                                  "price": 0,
                                  "currency": "EUR",
                                  "language": "es",
                                  "categoryId": "cat-workshops",
                                  "themeIds": ["theme-yoga"],
                                  "remindersEnabled": true
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private AuthSession register(String prefix) throws Exception {
        String email = prefix + "." + UUID.randomUUID() + "@karma.app";
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "secret123",
                                  "firstName": "Test",
                                  "lastName": "User"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return new AuthSession(node.get("user").get("id").asText(), node.get("accessToken").asText());
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
