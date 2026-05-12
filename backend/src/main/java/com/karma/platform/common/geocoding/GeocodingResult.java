package com.karma.platform.common.geocoding;

public record GeocodingResult(
        String normalizedQuery,
        String formattedAddress,
        String city,
        String country,
        double latitude,
        double longitude,
        String provider
) {
}
