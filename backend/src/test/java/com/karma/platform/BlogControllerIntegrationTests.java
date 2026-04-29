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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BlogControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanCreateAndPublishFeaturedBlogPost() throws Exception {
        String adminToken = login("admin@karma.app", "admin123");

        String createResponse = mockMvc.perform(post("/api/blog")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titleEs": "Guia de respiracion para eventos conscientes",
                                  "titleEn": "Breathwork guide for conscious events",
                                  "excerptEs": "Ideas practicas para llegar regulado a un evento.",
                                  "excerptEn": "Practical ideas to arrive regulated at an event.",
                                  "contentEs": "Contenido completo en espanol para la guia.",
                                  "contentEn": "Full English content for the guide.",
                                  "coverImageUrl": "https://cdn.karma.app/blog/breathwork.jpg",
                                  "featured": true,
                                  "published": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("guia-de-respiracion-para-eventos-conscientes"))
                .andExpect(jsonPath("$.featured").value(true))
                .andExpect(jsonPath("$.published").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(createResponse);
        String postId = created.get("id").asText();
        String slug = created.get("slug").asText();

        mockMvc.perform(get("/api/blog/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + postId + "')]").exists());

        mockMvc.perform(get("/api/blog/{slug}", slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleEs").value("Guia de respiracion para eventos conscientes"))
                .andExpect(jsonPath("$.contentEn").value("Full English content for the guide."));

        mockMvc.perform(put("/api/blog/{id}", postId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titleEs": "Guia de respiracion actualizada",
                                  "titleEn": "Updated breathwork guide",
                                  "excerptEs": "Nueva version del resumen.",
                                  "excerptEn": "Updated summary version.",
                                  "contentEs": "Contenido actualizado en espanol.",
                                  "contentEn": "Updated content in English.",
                                  "coverImageUrl": "https://cdn.karma.app/blog/breathwork-v2.jpg",
                                  "featured": false,
                                  "published": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleEs").value("Guia de respiracion actualizada"))
                .andExpect(jsonPath("$.featured").value(false));
    }

    @Test
    void nonAdminCannotCreateBlogPost() throws Exception {
        String userToken = login("demo@karma.app", "demo123");

        mockMvc.perform(post("/api/blog")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titleEs": "Intento sin permisos",
                                  "titleEn": "Unauthorized attempt",
                                  "excerptEs": "No deberia funcionar",
                                  "excerptEn": "This should not work",
                                  "contentEs": "Contenido",
                                  "contentEn": "Content",
                                  "coverImageUrl": null,
                                  "featured": false,
                                  "published": true
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Se requieren permisos de administrador"));
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
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("accessToken").asText();
    }
}
