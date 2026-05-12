package com.karma.platform.common.geocoding;

import java.util.Optional;

public class DomainGeocodingService {

    private final GeocodingService geocodingService;
    private final AddressFormatter addressFormatter;

    public DomainGeocodingService(GeocodingService geocodingService, AddressFormatter addressFormatter) {
        this.geocodingService = geocodingService;
        this.addressFormatter = addressFormatter;
    }

    public Optional<GeocodingResult> geocodeEventAddress(String venue, String addressLine, String city, String country) {
        return geocodingService.geocode(addressFormatter.format(venue, addressLine, city, country));
    }

    public Optional<GeocodingResult> geocodeGroupLocation(String city, String country) {
        return geocodingService.geocode(addressFormatter.format(city, country));
    }

    public Optional<GeocodingResult> geocodePreferredLocation(String preferredLocation) {
        return geocodingService.geocode(preferredLocation);
    }
}
