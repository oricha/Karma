package com.karma.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 6, max = 120) String password,
            @NotBlank String firstName,
            @NotBlank String lastName
    ) {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record ForgotPasswordRequest(@Email @NotBlank String email) {
    }

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 6, max = 120) String password
    ) {
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            UserDtos.UserResponse user,
            String emailVerificationToken
    ) {
    }

    public record ActionResponse(
            String message,
            String token
    ) {
    }

    public record VerificationResponse(
            String message,
            UserDtos.UserResponse user
    ) {
    }
}
