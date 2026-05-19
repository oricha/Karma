package com.karma.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karma.platform.persistence.entity.UserEntity;
import com.karma.platform.persistence.repository.PasswordResetTokenRepository;
import com.karma.platform.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void loginReturnsTokensForDemoUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "demo@karma.app",
                                  "password": "demo123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.user.email").value("demo@karma.app"));
    }

    @Test
    void registerForgotResetAndVerifyFlowWorks() throws Exception {
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new.user@karma.app",
                                  "password": "secret123",
                                  "firstName": "New",
                                  "lastName": "User"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerificationToken").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode registration = objectMapper.readTree(registerResponse);
        String verificationToken = registration.get("emailVerificationToken").asText();
        assertThat(verificationToken).isNotBlank();

        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", verificationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.emailVerified").value(true));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new.user@karma.app"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the account exists, a password reset email has been queued"));

        UserEntity registeredUser = userRepository.findByEmailIgnoreCase("new.user@karma.app").orElseThrow();
        String resetToken = passwordResetTokenRepository.findAll().stream()
                .filter(token -> registeredUser.getId().equals(token.getUserId()) && token.getUsedAt() == null)
                .map(token -> token.getToken())
                .findFirst()
                .orElseThrow();
        assertThat(resetToken).isNotBlank();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s",
                                  "password": "updated123"
                                }
                                """.formatted(resetToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new.user@karma.app",
                                  "password": "updated123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("new.user@karma.app"));
    }
}
