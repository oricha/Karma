package com.karma.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karma.platform.common.geocoding.DomainGeocodingService;
import com.karma.platform.common.geocoding.GeocodingResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GroupControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DomainGeocodingService domainGeocodingService;

    @Test
    void organizerCanCreateAndArchiveGroup() throws Exception {
        when(domainGeocodingService.geocodeGroupLocation("Malaga", "Espana"))
                .thenReturn(Optional.of(new GeocodingResult("malaga espana", "Malaga, Spain", "Malaga", "Spain", 36.7213, -4.4214, "mock")));

        String organizerToken = login("maria@karma.app", "password123");

        String createResponse = mockMvc.perform(post("/api/organizers/me/groups")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Circulo Malaga Consciente",
                                  "description": "Grupo de encuentros conscientes",
                                  "categoryId": "cat-workshops",
                                  "bannerUrl": "https://cdn.karma.app/groups/malaga.jpg",
                                  "city": "Malaga",
                                  "country": "Espana",
                                  "isPrivate": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Circulo Malaga Consciente"))
                .andExpect(jsonPath("$.slug").value("circulo-malaga-consciente"))
                .andExpect(jsonPath("$.isPrivate").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String groupId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(delete("/api/organizers/me/groups/{id}", groupId)
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/organizers/me/groups")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + groupId + "')].status").value(org.hamcrest.Matchers.hasItem("ARCHIVED")));
    }

    @Test
    void privateGroupJoinCreatesPendingMembershipAndOrganizerCanApprove() throws Exception {
        when(domainGeocodingService.geocodeGroupLocation("Sevilla", "Espana"))
                .thenReturn(Optional.of(new GeocodingResult("sevilla espana", "Sevilla, Spain", "Sevilla", "Spain", 37.3891, -5.9845, "mock")));

        String organizerToken = login("maria@karma.app", "password123");
        String memberToken = login("demo@karma.app", "demo123");

        String createResponse = mockMvc.perform(post("/api/organizers/me/groups")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Comunidad Privada Sevilla",
                                  "description": "Grupo privado de prueba",
                                  "categoryId": "cat-ceremonies",
                                  "bannerUrl": null,
                                  "city": "Sevilla",
                                  "country": "Espana",
                                  "isPrivate": true
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode group = objectMapper.readTree(createResponse);
        String groupId = group.get("id").asText();

        mockMvc.perform(post("/api/groups/{id}/join", groupId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/organizers/me/groups/{id}/memberships", groupId)
                        .param("status", "PENDING")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].user.email").value("demo@karma.app"));

        mockMvc.perform(post("/api/organizers/me/groups/{id}/members/{userId}/approve", groupId, "user-3")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/api/groups/{slug}", group.get("slug").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[?(@.email=='demo@karma.app')]").exists());
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
