package com.karma.platform.dto;

import com.karma.platform.model.GroupNotificationPreference;
import com.karma.platform.model.NewsletterFrequency;
import com.karma.platform.model.UserRole;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class UserDtos {

    private UserDtos() {
    }

    public record UserResponse(
            String id,
            String email,
            String firstName,
            String lastName,
            String avatarUrl,
            String bio,
            String phone,
            UserRole role,
            String locale,
            boolean emailVerified
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String email,
            String phone,
            String bio,
            String locale
    ) {
    }

    public record UserPreferenceResponse(
            NewsletterFrequency newsletterFrequency,
            boolean reviewReminders,
            String preferredLocation,
            double latitude,
            double longitude,
            int locationRadiusKm,
            List<String> themeIds
    ) {
    }

    public record UpdatePreferenceRequest(
            NewsletterFrequency newsletterFrequency,
            boolean reviewReminders,
            String preferredLocation,
            double latitude,
            double longitude,
            @Min(10) @Max(100) int locationRadiusKm,
            List<String> themeIds
    ) {
    }

    public record UpdateThemePreferencesRequest(List<String> themeIds) {
    }

    public record UpdateGroupNotificationRequest(GroupNotificationPreference preference) {
    }
}
