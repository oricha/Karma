package com.karma.platform.service;

import com.karma.platform.auth.JwtService;
import com.karma.platform.common.ApiException;
import com.karma.platform.dto.AuthDtos;
import com.karma.platform.model.User;
import com.karma.platform.seed.PlatformDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PlatformDataStore dataStore;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApiMapper apiMapper;

    public AuthService(PlatformDataStore dataStore, PasswordEncoder passwordEncoder, JwtService jwtService, ApiMapper apiMapper) {
        this.dataStore = dataStore;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.apiMapper = apiMapper;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (dataStore.findUserByEmail(request.email()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = dataStore.register(request.email(), request.password(), request.firstName(), request.lastName());
        return tokensFor(user, dataStore.createEmailVerificationToken(user.id()));
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = dataStore.findUserByEmail(request.email())
                .filter(item -> passwordEncoder.matches(request.password(), item.passwordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return tokensFor(user, null);
    }

    public AuthDtos.AuthResponse refresh(AuthDtos.RefreshRequest request) {
        String userId = dataStore.consumeRefreshTokenOwner(request.refreshToken())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid"));
        User user = dataStore.findUserById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        jwtService.parse(request.refreshToken());
        return tokensFor(user, null);
    }

    public AuthDtos.ActionResponse forgotPassword(AuthDtos.ForgotPasswordRequest request) {
        return dataStore.findUserByEmail(request.email())
                .map(user -> new AuthDtos.ActionResponse("Password reset token created", dataStore.createPasswordResetToken(user.id())))
                .orElse(new AuthDtos.ActionResponse("If the account exists, a password reset email has been queued", null));
    }

    public AuthDtos.ActionResponse resetPassword(AuthDtos.ResetPasswordRequest request) {
        String userId = dataStore.consumePasswordResetToken(request.token())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Reset token is invalid or expired"));
        User user = dataStore.findUserById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        dataStore.saveUser(new User(
                user.id(),
                user.email(),
                passwordEncoder.encode(request.password()),
                user.firstName(),
                user.lastName(),
                user.avatarUrl(),
                user.bio(),
                user.phone(),
                user.role(),
                user.locale(),
                user.emailVerified()
        ));
        return new AuthDtos.ActionResponse("Password updated successfully", null);
    }

    public AuthDtos.VerificationResponse verifyEmail(String token) {
        String userId = dataStore.consumeEmailVerificationToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Verification token is invalid or expired"));
        User user = dataStore.findUserById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        User verified = new User(
                user.id(),
                user.email(),
                user.passwordHash(),
                user.firstName(),
                user.lastName(),
                user.avatarUrl(),
                user.bio(),
                user.phone(),
                user.role(),
                user.locale(),
                true
        );
        dataStore.saveUser(verified);
        return new AuthDtos.VerificationResponse("Email verified successfully", apiMapper.toUser(verified));
    }

    private AuthDtos.AuthResponse tokensFor(User user, String emailVerificationToken) {
        String accessToken = jwtService.createAccessToken(user.id(), user.email(), user.role().name());
        String refreshToken = jwtService.createRefreshToken(user.id(), user.email(), user.role().name());
        dataStore.storeRefreshToken(refreshToken, user.id());
        return new AuthDtos.AuthResponse(accessToken, refreshToken, apiMapper.toUser(user), emailVerificationToken);
    }
}
