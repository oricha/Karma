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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventReviewIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void attendeeCanCreateUpdateAndDeleteReviewAndEventDetailReflectsAggregates() throws Exception {
        String attendeeToken = registerAndAttendEvent("reviews");

        mockMvc.perform(post("/api/events/event-1/reviews")
                        .header("Authorization", "Bearer " + attendeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 5,
                                  "comment": "Experiencia preciosa"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Experiencia preciosa"));

        mockMvc.perform(post("/api/events/event-1/reviews")
                        .header("Authorization", "Bearer " + attendeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 4,
                                  "comment": "Duplicada"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe una resena para este evento"));

        mockMvc.perform(put("/api/events/event-1/reviews")
                        .header("Authorization", "Bearer " + attendeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 4,
                                  "comment": "Actualizada"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Actualizada"));

        mockMvc.perform(get("/api/events/danza-extatica-atardecer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.averageRating").value(4.0))
                .andExpect(jsonPath("$.event.reviewCount").value(1))
                .andExpect(jsonPath("$.reviews[0].author.email").doesNotExist())
                .andExpect(jsonPath("$.reviews[0].author.firstName").value("Review"))
                .andExpect(jsonPath("$.reviews[0].rating").value(4));

        mockMvc.perform(delete("/api/events/event-1/reviews")
                        .header("Authorization", "Bearer " + attendeeToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/events/danza-extatica-atardecer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.reviewCount").value(0))
                .andExpect(jsonPath("$.reviews").isEmpty());
    }

    @Test
    void nonAttendeeCannotLeaveReview() throws Exception {
        String accessToken = register("blocked");

        mockMvc.perform(post("/api/events/event-1/reviews")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 2,
                                  "comment": "No deberia poder"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Debes asistir al evento antes de dejar una resena"));
    }

    private String registerAndAttendEvent(String prefix) throws Exception {
        String accessToken = register(prefix);
        mockMvc.perform(post("/api/events/event-1/rsvp")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
        return accessToken;
    }

    private String register(String prefix) throws Exception {
        String email = prefix + "." + UUID.randomUUID() + "@karma.app";
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "secret123",
                                  "firstName": "Review",
                                  "lastName": "User"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("accessToken").asText();
    }
}
