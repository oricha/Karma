package com.karma.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "server.forward-headers-strategy=framework"
})
@AutoConfigureMockMvc
class SecurityHardeningIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicResponsesExposeSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("X-Forwarded-Proto", "https"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Content-Security-Policy", org.hamcrest.Matchers.containsString("default-src 'self'")))
                .andExpect(header().string("Strict-Transport-Security", org.hamcrest.Matchers.containsString("max-age=31536000")));
    }

    @Test
    void invalidJwtCannotBypassAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("X-Forwarded-Proto", "https")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.value"))
                .andExpect(status().isForbidden());
    }

    @Test
    void swaggerAndHealthEndpointsArePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .header("X-Forwarded-Proto", "https"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").isString());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void corsIsRestrictedToConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/categories")
                        .header("X-Forwarded-Proto", "https")
                        .header(HttpHeaders.ORIGIN, "http://localhost:8080")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8080"));

        mockMvc.perform(options("/api/categories")
                        .header("X-Forwarded-Proto", "https")
                        .header(HttpHeaders.ORIGIN, "https://malicious.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrationStillWorksOverSecureProxyHeaders() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .header("X-Forwarded-Proto", "https")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "phase6.security@karma.app",
                                  "password": "secret123",
                                  "firstName": "Phase",
                                  "lastName": "Six"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("phase6.security@karma.app"));
    }
}
