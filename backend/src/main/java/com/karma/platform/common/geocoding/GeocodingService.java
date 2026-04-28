package com.karma.platform.common.geocoding;

import java.util.Optional;

public interface GeocodingService {

    Optional<GeocodingResult> geocode(String rawAddress);
}
