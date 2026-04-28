package com.karma.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrganizerControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void organizerDashboardReturnsKpisAndRecentActivity() throws Exception {
        String attendeeToken = login("demo@karma.app", "demo123");
        mockMvc.perform(post("/api/events/event-1/reviews")
                        .header("Authorization", "Bearer " + attendeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 5,
                                  "comment": "Gran evento"
                                }
                                """))
                .andExpect(status().isOk());

        String organizerToken = login("maria@karma.app", "password123");

        mockMvc.perform(get("/api/organizers/me/dashboard")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upcomingEvents").isNumber())
                .andExpect(jsonPath("$.totalRsvps").value(1))
                .andExpect(jsonPath("$.totalTicketsSold").value(1))
                .andExpect(jsonPath("$.totalRevenue").value(0.0))
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.totalReviews").value(1))
                .andExpect(jsonPath("$.recentEvents[0].id").value("event-1"))
                .andExpect(jsonPath("$.recentActivity[0].type").exists());
    }

    @Test
    void nonOrganizerCannotAccessDashboard() throws Exception {
        String userToken = login("demo@karma.app", "demo123");

        mockMvc.perform(get("/api/organizers/me/dashboard")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Perfil de organizer no encontrado"));
    }

    private String login(String email, String password) throws Exception {
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
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
