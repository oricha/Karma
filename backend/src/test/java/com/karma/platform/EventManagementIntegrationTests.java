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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventManagementIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void organizerCanCreateUpdateAndCancelEvent() throws Exception {
        String organizerToken = login("maria@karma.app", "password123");

        String createResponse = mockMvc.perform(post("/api/organizers/me/events")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupId": "group-1",
                                  "title": "Concierto Meditativo Madrid",
                                  "description": "Viaje sonoro de prueba",
                                  "coverImageUrl": null,
                                  "startDate": "2026-11-10T19:00:00",
                                  "endDate": "2026-11-10T21:00:00",
                                  "venueName": "Sala Luz",
                                  "address": "Gran Via 10",
                                  "city": "Madrid",
                                  "country": "Espana",
                                  "latitude": 40.4168,
                                  "longitude": -3.7038,
                                  "isOnline": false,
                                  "isHybrid": false,
                                  "onlineUrl": null,
                                  "status": "PUBLISHED",
                                  "featured": true,
                                  "maxAttendees": 25,
                                  "isFree": true,
                                  "price": 0,
                                  "currency": "EUR",
                                  "language": "es",
                                  "categoryId": "cat-music",
                                  "themeIds": ["theme-kirtan"],
                                  "remindersEnabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Concierto Meditativo Madrid"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String eventId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(put("/api/organizers/me/events/{id}", eventId)
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupId": "group-1",
                                  "title": "Concierto Meditativo Actualizado",
                                  "description": "Descripcion actualizada",
                                  "coverImageUrl": "https://cdn.karma.app/events/concierto.jpg",
                                  "startDate": "2026-11-11T19:00:00",
                                  "endDate": "2026-11-11T21:30:00",
                                  "venueName": "Sala Luz",
                                  "address": "Gran Via 10",
                                  "city": "Madrid",
                                  "country": "Espana",
                                  "latitude": 40.4168,
                                  "longitude": -3.7038,
                                  "isOnline": false,
                                  "isHybrid": false,
                                  "onlineUrl": null,
                                  "status": "PUBLISHED",
                                  "featured": false,
                                  "maxAttendees": 30,
                                  "isFree": true,
                                  "price": 0,
                                  "currency": "EUR",
                                  "language": "es",
                                  "categoryId": "cat-music",
                                  "themeIds": ["theme-kirtan"],
                                  "remindersEnabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Concierto Meditativo Actualizado"))
                .andExpect(jsonPath("$.maxAttendees").value(30));

        mockMvc.perform(get("/api/organizers/me/events")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + eventId + "')].title").value(hasItem("Concierto Meditativo Actualizado")));

        mockMvc.perform(delete("/api/organizers/me/events/{id}", eventId)
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/organizers/me/events")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + eventId + "')].status").value(hasItem("CANCELLED")));
    }

    @Test
    void eventCreationValidatesCapacity() throws Exception {
        String organizerToken = login("maria@karma.app", "password123");

        mockMvc.perform(post("/api/organizers/me/events")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupId": "group-1",
                                  "title": "Evento Invalido",
                                  "description": "Capacidad invalida",
                                  "startDate": "2026-09-10T19:00:00",
                                  "city": "Madrid",
                                  "country": "Espana",
                                  "latitude": 40.4168,
                                  "longitude": -3.7038,
                                  "isOnline": false,
                                  "isHybrid": false,
                                  "featured": false,
                                  "maxAttendees": 0,
                                  "isFree": true,
                                  "price": 0,
                                  "currency": "EUR",
                                  "language": "es",
                                  "categoryId": "cat-workshops",
                                  "themeIds": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicEventEndpointsSupportSearchSortAndNearbyQueries() throws Exception {
        String organizerToken = login("maria@karma.app", "password123");

        createEvent(organizerToken, "Madrid Sound Bath", "2026-11-01T18:00:00", "Madrid", "Espana", 40.4168, -3.7038, "cat-music");
        createEvent(organizerToken, "Sevilla Breathwork Journey", "2026-12-01T18:00:00", "Sevilla", "Espana", 37.3891, -5.9845, "cat-workshops");

        mockMvc.perform(get("/api/events")
                        .param("q", "Journey"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Sevilla Breathwork Journey"));

        mockMvc.perform(get("/api/events")
                        .param("sort", "date_desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Sevilla Breathwork Journey"));

        mockMvc.perform(get("/api/events/nearby")
                        .param("lat", "40.4168")
                        .param("lng", "-3.7038")
                        .param("radiusKm", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title").value(hasItem("Madrid Sound Bath")))
                .andExpect(jsonPath("$[*].title").value(not(hasItem("Sevilla Breathwork Journey"))));
    }

    private void createEvent(
            String organizerToken,
            String title,
            String startDate,
            String city,
            String country,
            double latitude,
            double longitude,
            String categoryId
    ) throws Exception {
        mockMvc.perform(post("/api/organizers/me/events")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupId": "group-1",
                                  "title": "%s",
                                  "description": "Evento de prueba %s",
                                  "coverImageUrl": null,
                                  "startDate": "%s",
                                  "endDate": "2026-12-01T21:00:00",
                                  "venueName": "Sala Karma",
                                  "address": "Direccion 1",
                                  "city": "%s",
                                  "country": "%s",
                                  "latitude": %s,
                                  "longitude": %s,
                                  "isOnline": false,
                                  "isHybrid": false,
                                  "onlineUrl": null,
                                  "status": "PUBLISHED",
                                  "featured": false,
                                  "maxAttendees": 40,
                                  "isFree": true,
                                  "price": 0,
                                  "currency": "EUR",
                                  "language": "es",
                                  "categoryId": "%s",
                                  "themeIds": ["theme-yoga"],
                                  "remindersEnabled": true
                                }
                                """.formatted(title, UUID.randomUUID(), startDate, city, country, latitude, longitude, categoryId)))
                .andExpect(status().isOk());
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
        JsonNode node = objectMapper.readTree(response);
        return node.get("accessToken").asText();
    }
}
