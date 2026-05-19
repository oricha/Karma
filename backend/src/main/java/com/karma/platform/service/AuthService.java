package com.karma.platform.service;

import com.karma.platform.auth.JwtService;
import com.karma.platform.common.ApiException;
import com.karma.platform.dto.AuthDtos;
import com.karma.platform.model.NewsletterFrequency;
import com.karma.platform.model.UserRole;
import com.karma.platform.persistence.entity.*;
import com.karma.platform.persistence.repository.*;
import com.karma.platform.service.notification.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserThemePreferenceRepository userThemePreferenceRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApiMapper apiMapper;
    private final EmailService emailService;
    private final long refreshTokenDays;

    public AuthService(
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository,
            UserThemePreferenceRepository userThemePreferenceRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ApiMapper apiMapper,
            EmailService emailService,
            @Value("${karma.jwt.refresh-token-days}") long refreshTokenDays
    ) {
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.userThemePreferenceRepository = userThemePreferenceRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.apiMapper = apiMapper;
        this.emailService = emailService;
        this.refreshTokenDays = refreshTokenDays;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "error.email-exists", "Email is already registered");
        }

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(UserRole.USER);
        user.setLocale("es");
        user.setEmailVerified(false);
        userRepository.save(user);

        UserPreferenceEntity preference = new UserPreferenceEntity();
        preference.setUserId(user.getId());
        preference.setNewsletterFrequency(NewsletterFrequency.WEEKLY);
        preference.setReviewReminders(true);
        preference.setPreferredLocation("Madrid");
        preference.setLatitude(40.4168);
        preference.setLongitude(-3.7038);
        preference.setLocationRadiusKm(50);
        userPreferenceRepository.save(preference);
        replaceThemePreferences(user.getId(), List.of("theme-yoga", "theme-ecstatic"));

        String emailVerificationToken = createEmailVerificationToken(user.getId());
        emailService.sendWelcomeEmail(user);
        emailService.sendEmailVerificationEmail(user, emailVerificationToken);
        return tokensFor(user, emailVerificationToken);
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        UserEntity user = userRepository.findByEmailIgnoreCase(request.email())
                .filter(item -> passwordEncoder.matches(request.password(), item.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "error.bad-credentials", "Invalid email or password"));
        return tokensFor(user, null);
    }

    @Transactional
    public AuthDtos.AuthResponse refresh(AuthDtos.RefreshRequest request) {
        RefreshTokenEntity refreshToken = refreshTokenRepository.findById(request.refreshToken())
                .filter(item -> item.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "error.refresh-token-invalid", "Refresh token is invalid"));
        UserEntity user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "error.user-not-found", "User not found"));
        jwtService.parse(request.refreshToken());
        return tokensFor(user, null);
    }

    @Transactional
    public AuthDtos.ActionResponse forgotPassword(AuthDtos.ForgotPasswordRequest request) {
        return userRepository.findByEmailIgnoreCase(request.email())
                .map(user -> {
                    String resetToken = createPasswordResetToken(user.getId());
                    emailService.sendPasswordResetEmail(user, resetToken);
                    return new AuthDtos.ActionResponse("If the account exists, a password reset email has been queued", null);
                })
                .orElse(new AuthDtos.ActionResponse("If the account exists, a password reset email has been queued", null));
    }

    @Transactional
    public AuthDtos.ActionResponse resetPassword(AuthDtos.ResetPasswordRequest request) {
        PasswordResetTokenEntity token = passwordResetTokenRepository.findById(request.token())
                .filter(item -> item.getUsedAt() == null && item.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "error.reset-token-invalid", "Reset token is invalid or expired"));
        UserEntity user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found", "User not found"));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);
        return new AuthDtos.ActionResponse("Password updated successfully", null);
    }

    @Transactional
    public AuthDtos.VerificationResponse verifyEmail(String tokenValue) {
        EmailVerificationTokenEntity token = emailVerificationTokenRepository.findById(tokenValue)
                .filter(item -> item.getUsedAt() == null && item.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "error.verification-token-invalid", "Verification token is invalid or expired"));
        UserEntity user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found", "User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
        token.setUsedAt(LocalDateTime.now());
        emailVerificationTokenRepository.save(token);
        return new AuthDtos.VerificationResponse("Email verified successfully", apiMapper.toUser(user));
    }

    private AuthDtos.AuthResponse tokensFor(UserEntity user, String emailVerificationToken) {
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        String accessToken = jwtService.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.createRefreshToken(user.getId(), user.getEmail(), user.getRole().name());

        RefreshTokenEntity tokenEntity = new RefreshTokenEntity();
        tokenEntity.setToken(refreshToken);
        tokenEntity.setUserId(user.getId());
        tokenEntity.setCreatedAt(LocalDateTime.now());
        tokenEntity.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenDays));
        refreshTokenRepository.save(tokenEntity);

        return new AuthDtos.AuthResponse(accessToken, refreshToken, apiMapper.toUser(user), emailVerificationToken);
    }

    private String createPasswordResetToken(String userId) {
        passwordResetTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setToken(tokenValue);
        token.setUserId(userId);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        passwordResetTokenRepository.save(token);
        return tokenValue;
    }

    private String createEmailVerificationToken(String userId) {
        emailVerificationTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        String tokenValue = UUID.randomUUID().toString();
        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setToken(tokenValue);
        token.setUserId(userId);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiryDate(LocalDateTime.now().plusDays(2));
        emailVerificationTokenRepository.save(token);
        return tokenValue;
    }

    private void replaceThemePreferences(String userId, List<String> themeIds) {
        userThemePreferenceRepository.deleteByUserId(userId);
        themeIds.forEach(themeId -> {
            UserThemePreferenceEntity preference = new UserThemePreferenceEntity();
            preference.setUserId(userId);
            preference.setThemeId(themeId);
            userThemePreferenceRepository.save(preference);
        });
    }
}
