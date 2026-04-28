package com.karma.platform.config;

import com.karma.platform.common.geocoding.AddressFormatter;
import com.karma.platform.common.geocoding.DefaultAddressFormatter;
import com.karma.platform.common.geocoding.DomainGeocodingService;
import com.karma.platform.common.geocoding.GeocodingService;
import com.karma.platform.common.geocoding.NoopGeocodingService;
import com.karma.platform.common.geocoding.NominatimGeocodingService;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class GeocodingConfig {

    @Bean
    AddressFormatter addressFormatter() {
        return new DefaultAddressFormatter();
    }

    @Bean
    GeocodingService geocodingService(KarmaGeocodingProperties properties, AddressFormatter addressFormatter, MessageSource messageSource) {
        if (!properties.enabled()) {
            return new NoopGeocodingService(messageSource);
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.timeoutMillis());
        requestFactory.setReadTimeout(properties.timeoutMillis());

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", properties.userAgent())
                .build();

        return new NominatimGeocodingService(restClient, properties, addressFormatter, messageSource);
    }

    @Bean
    DomainGeocodingService domainGeocodingService(GeocodingService geocodingService, AddressFormatter addressFormatter) {
        return new DomainGeocodingService(geocodingService, addressFormatter);
    }
}
