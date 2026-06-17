package com.karma.platform.auth;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    @Test
    void acceptsShortSecretsByDerivingA256BitKey() {
        JwtService jwtService = new JwtService(
                "short-secret-key",
                "karma-platform",
                "karma-platform-api",
                15,
                7
        );

        String token = jwtService.createAccessToken("user-1", "demo@karma.app", "USER");
        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("user-1");
        assertThat(claims.get("email", String.class)).isEqualTo("demo@karma.app");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    void rejectsBlankSecret() {
        assertThatThrownBy(() -> new JwtService(
                "   ",
                "karma-platform",
                "karma-platform-api",
                15,
                7
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("karma.jwt.secret must not be blank");
    }
}
