package com.karma.platform.model;

import java.util.List;

public record UserPreference(
        String userId,
        NewsletterFrequency newsletterFrequency,
        boolean reviewReminders,
        String preferredLocation,
        double latitude,
        double longitude,
        int locationRadiusKm,
        List<String> themeIds
) {
}
