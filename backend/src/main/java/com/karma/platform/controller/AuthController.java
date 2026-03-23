package com.karma.platform.controller;

import com.karma.platform.dto.AuthDtos;
import com.karma.platform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthDtos.AuthResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/forgot-password")
    public AuthDtos.ActionResponse forgotPassword(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public AuthDtos.ActionResponse resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @GetMapping("/verify-email")
    public AuthDtos.VerificationResponse verifyEmail(@RequestParam String token) {
        return authService.verifyEmail(token);
    }
}
