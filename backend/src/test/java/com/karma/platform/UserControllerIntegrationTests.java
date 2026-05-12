package com.karma.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karma.platform.common.geocoding.DomainGeocodingService;
import com.karma.platform.common.geocoding.GeocodingResult;
import com.karma.platform.common.storage.FileStorageService;
import com.karma.platform.common.storage.StoredFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private DomainGeocodingService domainGeocodingService;

    @Test
    void updatesProfileWithoutChangingEmail() throws Exception {
        String accessToken = registerAndGetAccessToken("profile");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Perfil",
                                  "lastName": "Actualizado",
                                  "email": "profile.user@karma.app",
                                  "phone": "+34000000001",
                                  "bio": "Bio actualizada",
                                  "locale": "en"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Perfil"))
                .andExpect(jsonPath("$.lastName").value("Actualizado"))
                .andExpect(jsonPath("$.locale").value("en"))
                .andExpect(jsonPath("$.phone").value("+34000000001"));
    }

    @Test
    void updatesPreferencesUsingGeocodingWhenAvailable() throws Exception {
        String accessToken = registerAndGetAccessToken("prefs");
        when(domainGeocodingService.geocodePreferredLocation("Sevilla, España"))
                .thenReturn(Optional.of(new GeocodingResult(
                        "sevilla espana",
                        "Sevilla, Spain",
                        "Sevilla",
                        "Spain",
                        37.3891,
                        -5.9845,
                        "mock"
                )));

        mockMvc.perform(put("/api/users/me/preferences")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newsletterFrequency": "MONTHLY",
                                  "reviewReminders": true,
                                  "preferredLocation": "Sevilla, España",
                                  "latitude": 0.0,
                                  "longitude": 0.0,
                                  "locationRadiusKm": 25,
                                  "themeIds": ["theme-yoga", "theme-ecstatic"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredLocation").value("Sevilla, Spain"))
                .andExpect(jsonPath("$.latitude").value(37.3891))
                .andExpect(jsonPath("$.longitude").value(-5.9845))
                .andExpect(jsonPath("$.locationRadiusKm").value(25));
    }

    @Test
    void uploadsAvatarAndReturnsUpdatedProfile() throws Exception {
        String accessToken = registerAndGetAccessToken("avatar");
        when(fileStorageService.upload(anyString(), any(byte[].class), eq("image/png")))
                .thenReturn(new StoredFile("bucket", "avatars/test.png", "image/png", 128, "https://cdn.karma.app/avatars/test.png"));

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "png-bytes".getBytes());

        mockMvc.perform(multipart("/api/users/me/avatar")
                        .file(file)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("https://cdn.karma.app/avatars/test.png"));
    }

    @Test
    void changesPasswordAndEmailThroughDedicatedEndpoints() throws Exception {
        String accessToken = registerAndGetAccessToken("security");

        mockMvc.perform(put("/api/users/me/password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "secret123",
                                  "newPassword": "updated456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contrasena actualizada correctamente"));

        mockMvc.perform(put("/api/users/me/email")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "security.updated@karma.app",
                                  "currentPassword": "updated456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("security.updated@karma.app"))
                .andExpect(jsonPath("$.user.emailVerified").value(false))
                .andExpect(jsonPath("$.emailVerificationToken").isString());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "security.updated@karma.app",
                                  "password": "updated456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("security.updated@karma.app"));
    }

    private String registerAndGetAccessToken(String prefix) throws Exception {
        String email = prefix + ".user@karma.app";
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "secret123",
                                  "firstName": "Test",
                                  "lastName": "User-%s"
                                }
                                """.formatted(email, UUID.randomUUID())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("accessToken").asText();
    }
}
