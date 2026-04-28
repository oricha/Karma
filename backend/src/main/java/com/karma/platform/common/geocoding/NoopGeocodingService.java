package com.karma.platform.common.geocoding;

import com.karma.platform.common.ApiException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

import java.util.Locale;
import java.util.Optional;

public class NoopGeocodingService implements GeocodingService {

    private final MessageSource messageSource;

    public NoopGeocodingService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public Optional<GeocodingResult> geocode(String rawAddress) {
        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "geocoding.disabled",
                messageSource.getMessage("geocoding.disabled", null, Locale.getDefault()));
    }
}
