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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GroupDiscussionIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void activeMemberCanCreateReplyAndOrganizerCanPinAndDeletePost() throws Exception {
        String memberToken = login("demo@karma.app", "demo123");
        String organizerToken = login("maria@karma.app", "password123");

        String createResponse = mockMvc.perform(post("/api/groups/group-1/posts")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "<b>Nos vemos esta tarde</b><script>alert('xss')</script>",
                                  "imageUrl": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author.firstName").value("Demo"))
                .andExpect(jsonPath("$.content").value("Nos vemos esta tarde"))
                .andExpect(jsonPath("$.pinned").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String postId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(post("/api/groups/group-1/posts/{postId}/replies", postId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Perfecto, abrimos puertas a las 18:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replyCount").value(1))
                .andExpect(jsonPath("$.replies[0].author.firstName").value("Demo"));

        mockMvc.perform(put("/api/groups/group-1/posts/{postId}/pin", postId)
                        .param("pinned", "true")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinned").value(true));

        mockMvc.perform(get("/api/groups/group-1/posts")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(postId))
                .andExpect(jsonPath("$[0].pinned").value(true))
                .andExpect(jsonPath("$[0].replyCount").value(1));

        mockMvc.perform(delete("/api/groups/group-1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/groups/group-1/posts")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void nonMemberCannotAccessGroupDiscussions() throws Exception {
        String outsiderToken = login("carlos@karma.app", "password123");

        mockMvc.perform(get("/api/groups/group-1/posts")
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Debes ser miembro activo para participar en el foro"));
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
