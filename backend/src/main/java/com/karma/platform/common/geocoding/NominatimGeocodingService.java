package com.karma.platform.common.geocoding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.karma.platform.config.KarmaGeocodingProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.context.MessageSource;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class NominatimGeocodingService implements GeocodingService {

    private final RestClient restClient;
    private final KarmaGeocodingProperties properties;
    private final AddressFormatter addressFormatter;
    private final MessageSource messageSource;
    private final Map<String, Optional<GeocodingResult>> cache;

    public NominatimGeocodingService(
            RestClient restClient,
            KarmaGeocodingProperties properties,
            AddressFormatter addressFormatter,
            MessageSource messageSource
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.addressFormatter = addressFormatter;
        this.messageSource = messageSource;
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Optional<GeocodingResult>> eldest) {
                return size() > properties.cacheSize();
            }
        };
    }

    @Override
    public Optional<GeocodingResult> geocode(String rawAddress) {
        String normalizedQuery = addressFormatter.normalize(rawAddress);
        if (normalizedQuery.isBlank()) {
            return Optional.empty();
        }
        synchronized (cache) {
            if (cache.containsKey(normalizedQuery)) {
                return cache.get(normalizedQuery);
            }
        }
        Optional<GeocodingResult> result = fetchWithRetry(rawAddress, normalizedQuery);
        synchronized (cache) {
            cache.put(normalizedQuery, result);
        }
        return result;
    }

    private Optional<GeocodingResult> fetchWithRetry(String rawAddress, String normalizedQuery) {
        RuntimeException lastException = null;
        for (int attempt = 0; attempt <= properties.maxRetries(); attempt++) {
            try {
                List<NominatimResponse> response = restClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/search")
                                .queryParam("q", rawAddress)
                                .queryParam("format", "jsonv2")
                                .queryParam("limit", 1)
                                .build())
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<NominatimResponse>>() {
                        });

                if (response == null || response.isEmpty()) {
                    return Optional.empty();
                }

                NominatimResponse item = response.getFirst();
                return Optional.of(new GeocodingResult(
                        normalizedQuery,
                        item.displayName(),
                        item.address() == null ? null : item.address().cityOrTown(),
                        item.address() == null ? null : item.address().country(),
                        Double.parseDouble(item.lat()),
                        Double.parseDouble(item.lon()),
                        properties.provider()
                ));
            } catch (RuntimeException exception) {
                lastException = exception;
            }
        }
        if (lastException != null) {
            throw lastException;
        }
        throw new IllegalStateException(messageSource.getMessage("geocoding.disabled", null, Locale.getDefault()));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimResponse(String lat, String lon, @JsonProperty("display_name") String displayName, Address address) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Address(String city, String town, String village, String country) {
        private String cityOrTown() {
            if (city != null && !city.isBlank()) {
                return city;
            }
            if (town != null && !town.isBlank()) {
                return town;
            }
            return village;
        }
    }
}
